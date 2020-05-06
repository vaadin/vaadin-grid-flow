const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const GRID_DIR = '../../';
const REF_GRID_DIR = './reference-clone';
const TEST_DIR = 'vaadin-grid-flow-integration-tests';

const gridPath = path.resolve(GRID_DIR);
const refGridPath = path.resolve(REF_GRID_DIR);

const gridTestPath = path.resolve(gridPath, TEST_DIR);
const refGridTestPath = path.resolve(refGridPath, TEST_DIR);

const resultsPath = path.resolve('./results');

const REF_JETTY_PORT = 8088;
// TODO: Fix
const REF_GIT_TAG = 'benchmark';

const processes = [];
const testVariants = [];

['firefox-headless', 'chrome-headless'].forEach((browserName) => {
  ['simple', 'multicolumn', 'componentrenderers', 'detailsopened'].forEach(
    (variantName) => {
      testVariants.push({
        variantName,
        browserName,
        metricName: 'rendertime',
      });
      testVariants.push({
        variantName,
        browserName,
        metricName: 'scrollframetime',
      });
    }
  );

  ['rendertime', 'scrollframetime', 'expandtime'].forEach((metricName) => {
    testVariants.push({
      variantName: 'tree',
      browserName,
      metricName,
    });

    testVariants.push({
      variantName: 'mixed',
      browserName,
      metricName,
    });
  });
});

const startJetty = (cwd) => {
  return new Promise((resolve) => {
    const jetty = spawn('mvn', ['jetty:run'], { cwd });
    processes.push(jetty);
    jetty.stderr.on('data', (data) => console.error(data.toString()));
    jetty.stdout.on('data', (data) => {
      console.log(data.toString());

      if (data.toString().includes('Started Jetty Server')) {
        resolve();
      }
    });
  });
};

const prepareReferenceGrid = () => {
  execSync(
    `git clone --depth=1 --single-branch --branch ${REF_GIT_TAG} https://github.com/vaadin/vaadin-grid-flow.git ${refGridPath}`
  );

  // Add Jetty config to start the server on a different port
  const pomFile = path.resolve(refGridTestPath, 'pom.xml');
  const pomFileContent = fs.readFileSync(pomFile, 'utf8');

  const result = pomFileContent.replace(
    /<artifactId>jetty-maven-plugin<\/artifactId>/g,
    `
    <artifactId>jetty-maven-plugin</artifactId>
      <configuration>
        <httpConnector>
          <port>${REF_JETTY_PORT}</port>
        </httpConnector>
        <stopPort>${REF_JETTY_PORT + 1}</stopPort>
      </configuration>
    `
  );

  fs.writeFileSync(pomFile, result, 'utf8');

  execSync(`mvn versions:set -DnewVersion=${REF_GIT_TAG}-BENCHMARK`, {
    cwd: refGridPath,
  });

  execSync(`mvn install -DskipTests`, { cwd: refGridPath });
};

const reportTestResults = (testVariantName, testResultsFilePath) => {
  const testResultsFileContent = fs.readFileSync(testResultsFilePath, 'utf-8');
  const { benchmarks } = JSON.parse(testResultsFileContent);
  const { low, high } = benchmarks[0].differences.find((d) => d).percentChange;
  const relativeDifferenceAverage = (low + high) / 2;

  // Print the test result as TeamCity build statistics
  console.log(
    `##teamcity[buildStatisticValue key='${testVariantName}' value='${relativeDifferenceAverage.toFixed(
      6
    )}']\n`
  );
};

const runTachometerTest = ({ variantName, metricName, browserName }) => {
  const sampleSize = {
    rendertime: 40,
    expandtime: 40,
    scrollframetime: 10,
  }[metricName];

  const testVariantName = `${variantName}-${metricName}-${browserName}`;
  const testResultsFilePath = path.resolve(
    resultsPath,
    `${testVariantName}.json`
  );
  const args = [];
  args.push('--measure', 'global');
  args.push('--sample-size', sampleSize);
  args.push('--json-file', testResultsFilePath);
  args.push('--browser', browserName);
  const ports = [9998, REF_JETTY_PORT];
  ports.forEach((port) => {
    args.push(
      `http://localhost:${port}/benchmark?variant=${variantName}&metric=${metricName}`
    );
  });

  return new Promise((resolve) => {
    const tach = spawn('node_modules/.bin/tach', args, { cwd: gridTestPath });
    tach.stderr.on('data', (data) => console.error(data.toString()));
    tach.stdout.on('data', (data) => console.log(data.toString()));
    tach.on('close', () => {
      reportTestResults(testVariantName, testResultsFilePath);
      resolve();
    });
  });
};

const run = async () => {
  if (!fs.existsSync(refGridPath)) {
    console.log('Prepare the reference Grid project');
    prepareReferenceGrid();
  }

  console.log('Starting the Jetty server: Grid');
  await startJetty(gridTestPath);

  console.log('Starting the Jetty server: reference Grid');
  await startJetty(refGridTestPath);

  if (!fs.existsSync('./node_modules/.bin/tach')) {
    console.log('Installing tachometer');
    execSync('npm i tachometer', { cwd: gridTestPath });
  }

  for (const testVariant of testVariants) {
    console.log(
      'Running test:',
      `${testVariant.variantName}-${testVariant.metricName}`
    );
    await runTachometerTest(testVariant);
  }

  // Exit
  processes.forEach((ps) => ps.kill());
  process.exit(0);
};

run();

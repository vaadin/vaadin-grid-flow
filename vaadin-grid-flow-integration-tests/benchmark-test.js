const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const REF_DIR = './benchmark/reference-clone';
const RESULTS_DIR = './benchmark/results';
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
    `git clone --depth=1 --single-branch --branch ${REF_GIT_TAG} https://github.com/vaadin/vaadin-grid-flow.git ${REF_DIR}`
  );

  const refDirPath = path.resolve(REF_DIR);
  // Add Jetty config to start the server on a different port
  const pomFile = `${refDirPath}/vaadin-grid-flow-integration-tests/pom.xml`;
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
    cwd: refDirPath,
  });

  execSync(`mvn install -DskipTests`, { cwd: refDirPath });
};

const runTachometerTest = (
  { variantName, metricName, browserName }
) => {

  const sampleSize = {
    'rendertime': 40,
    'expandtime': 40,
    'scrollframetime': 10
  }[metricName];

  const args = [];
  args.push('--measure', 'global');
  args.push('--sample-size', sampleSize);
  args.push(
    '--json-file',
    `${RESULTS_DIR}/${variantName}-${metricName}-${browserName}.json`
  );
  args.push('--browser', browserName);
  const ports = [9998, REF_JETTY_PORT];
  ports.forEach((port) => {
    args.push(
      `http://localhost:${port}/benchmark?variant=${variantName}&metric=${metricName}`
    );
  });

  if (!fs.existsSync('./node_modules/.bin/tach')) {
    execSync('npm i tachometer');
  }

  return new Promise((resolve) => {
    const tach = spawn('node_modules/.bin/tach', args);
    tach.stderr.on('data', (data) => console.error(data.toString()));
    tach.stdout.on('data', (data) => console.log(data.toString()));
    tach.on('close', resolve);
  });
};

const run = async () => {
  if (!fs.existsSync(path.resolve(REF_DIR))) {
    console.log('Prepare the reference Grid project');
    prepareReferenceGrid();
  }

  console.log('Starting the Jetty server: Grid');
  await startJetty('.');

  console.log('Starting the Jetty server: reference Grid');
  await startJetty(`${REF_DIR}/vaadin-grid-flow-integration-tests`);

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

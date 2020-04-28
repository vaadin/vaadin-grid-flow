const { spawn, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const REF_DIR = './benchmark/reference-clone';
const REF_JETTY_PORT = 8088;
const processes = [];
const testVariants = [];
['simple', 'emptycells', 'componentrenderers', 'detailsopened'].forEach(
  (variantName) => {
    testVariants.push({
      variantName,
      metricName: 'rendertime',
      sampleSize: 20,
    });
    testVariants.push({
      variantName,
      metricName: 'scrollframetime',
      sampleSize: 10,
    });
  }
);
testVariants.push({
  variantName: 'treegrid',
  metricName: 'nodeexpandtime',
  sampleSize: 10,
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
  const referenceGridBranch = '5.0.0';
  // TODO: Use a version that reports techometer results
  execSync(
    `git clone --depth=1 --single-branch --branch ${referenceGridBranch} https://github.com/vaadin/vaadin-grid-flow.git ${REF_DIR}`
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

  execSync(`mvn versions:set -DnewVersion=${referenceGridBranch}-BENCHMARK`, { cwd: refDirPath });

  execSync(`mvn install -DskipTests`, { cwd: refDirPath })
};

const runTachometerTest = ({ variantName, metricName, sampleSize }) => {
  const args = [];
  args.push('--measure', 'global');
  args.push('--sample-size', sampleSize);
  args.push('--json-file', `benchmark/${variantName}-${metricName}.json`);
  args.push('--browser', 'chrome-headless,firefox-headless');
  // const ports = [9998, REF_JETTY_PORT];
  const ports = [9998];
  ports.forEach((port) => {
    args.push(
      `http://localhost:${port}/benchmark?variant=${variantName}&metric=${metricName}`
    );
  });

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

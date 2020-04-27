const { spawn, exec, execSync } = require('child_process');
const fs = require('fs');

const TMP_DIR = 'tmp';
const REF_JETTY_PORT = 8088;

const startJetty = (cwd, port) => {
  // TODO: How to make Jetty use the port
  return new Promise((resolve) => {
    const jetty = spawn('mvn', ['jetty:run'], { cwd });
    jetty.stderr.on('data', (data) => console.error(data.toString()));
    jetty.stdout.on('data', (data) => {
      console.log(data.toString());

      if (data.toString().includes('Started Jetty Server')) {
        resolve();
      }
    });

  });
};

const rmTmpDir = () => fs.rmdirSync(TMP_DIR, { recursive: true });

const cloneReferenceGrid = () => {
  rmTmpDir();
  // TODO: Use a version that reports techometer results
  execSync(
    `git clone --depth=1 --single-branch --branch 5.0.0 https://github.com/vaadin/vaadin-grid-flow.git ${TMP_DIR}`
  );
};

const runTachometerTest = ({ variantName, metricName, sampleSize }) => {
  const args = [];
  args.push('--measure', 'global');
  args.push('--sample-size', sampleSize);
  args.push('--json-file', `benchmark/${variantName}-${metricName}.json`);
  args.push('--browser', 'chrome-headless,firefox-headless');
  // const ports = [9998, REF_JETTY_PORT];
  const ports = [9998];
  ports.forEach(port => {
    `http://localhost:${port}/benchmark?variant=${variantName}&metric=${metricName}`
  });
  const tach = spawn('node_modules/.bin/tach', args);

  tach.stdout.on('data', (data) => console.log(data.toString()));
  tach.stderr.on('data', (data) => console.error(data.toString()));

  return new Promise((resolve) => {
    // TODO
  });
};

const run = async () => {
  console.log('Cloning the reference Grid');
  // cloneReferenceGrid();

  console.log('Starting the Jetty server: Grid');
  await startJetty('.', 9998);

  console.log('Starting the Jetty server: reference Grid');
  // await startJetty(`${TMP_DIR}/vaadin-grid-integration-tests`, REF_JETTY_PORT);

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

  for (const testVariant of testVariants) {
    console.log(
      'Running test:',
      `${testVariant.variantName}-${testVariant.metricName}`
    );
    await runTachometerTest(testVariant);
  }

  // Remove the tmp clone
  rmTmpDir();

  // Exit
  process.exit(0);
};

run();

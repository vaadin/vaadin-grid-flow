const { spawn, exec, execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const REF_DIR = './tmp';
const REF_JETTY_PORT = 8088;

const startJetty = (cwd) => {
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

const rmTmpDir = () => fs.rmdirSync(REF_DIR, { recursive: true });

const cloneReferenceGrid = () => {
  rmTmpDir();
  // TODO: Use a version that reports techometer results
  execSync(
    `git clone --depth=1 --single-branch --branch 5.0.0 https://github.com/vaadin/vaadin-grid-flow.git ${REF_DIR}`
  );

  // Add Jetty config to start the server on a different port
  const pomFile = `${path.resolve(REF_DIR)}/vaadin-grid-flow-integration-tests/pom.xml`;
  const pomFileContent = fs.readFileSync(pomFile, 'utf8');

  const result = pomFileContent.replace(/<artifactId>jetty-maven-plugin<\/artifactId>/g, `
    <artifactId>jetty-maven-plugin</artifactId>
      <configuration>
        <httpConnector>
          <port>${REF_JETTY_PORT}</port>
        </httpConnector>
        <stopPort>${REF_JETTY_PORT + 1}</stopPort>
      </configuration>
    `);

  fs.writeFileSync(pomFile, result, 'utf8');
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
    args.push(`http://localhost:${port}/benchmark?variant=${variantName}&metric=${metricName}`);
  });

  return new Promise((resolve) => {
    const tach = spawn('node_modules/.bin/tach', args);
    tach.stderr.on('data', (data) => console.error(data.toString()));
    tach.stdout.on('data', (data) => console.log(data.toString()));
    tach.on('close', resolve);
  });
};

const run = async () => {
  console.log('Cloning the reference Grid');
  cloneReferenceGrid();

  console.log('Starting the Jetty server: Grid');
  await startJetty('.');

  console.log('Starting the Jetty server: reference Grid');
  await startJetty(`${REF_DIR}/vaadin-grid-flow-integration-tests`);

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

  if (!fs.existsSync('./benchmark')) {
    fs.mkdirSync('./benchmark');
  }

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

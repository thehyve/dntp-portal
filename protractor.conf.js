'use strict';

const fs = require('fs');

fs.mkdir('./target/e2e-reports', {recursive: true}, function(err) {
  if (err) {
    throw err;
  }
});

// An example configuration file.
exports.config = {
  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome'
  },
  chromeOptions: {
    args: ['--headless', '--disable-gpu', '--window-size=1280,1024']
  },

  // directConnect: true,

  // set to "custom" instead of cucumber.
  framework: 'custom',

  // path relative to the current config file
  frameworkPath: require.resolve('protractor-cucumber-framework'),

  // Spec patterns are relative to the current working directly when
  // protractor is called.
  specs: ['e2e/*.feature'],

  cucumberOpts: {
    require: ['e2e/util.js/', 'e2e/steps/*.js'],
    'no-colors': true,
    format: [
      'progress',
      'pretty:target/e2e-reports/results.txt',
      'json:target/e2e-reports/results.json'
    ]
  },

  resultJsonOutputFile: 'test-report.json'
};

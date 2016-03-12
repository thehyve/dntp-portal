'use strict';

var paths = require('./.yo-rc.json')['generator-gulp-angular'].props.paths;

// An example configuration file.
exports.config = {
  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome'
  },

  // set to "custom" instead of cucumber.
  framework: 'custom',

  // path relative to the current config file
  frameworkPath: require.resolve('protractor-cucumber-framework'),

  // Spec patterns are relative to the current working directly when
  // protractor is called.
  specs: [paths.e2e + '/*.feature'],

  cucumberOpts: {
    require: paths.e2e + '/steps/*.js'
  },

  resultJsonOutputFile: 'test-report.json'
};

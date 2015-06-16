'use strict';

var paths = require('./.yo-rc.json')['generator-gulp-angular'].props.paths;

// An example configuration file.
exports.config = {
  // Capabilities to be passed to the webdriver instance.
  capabilities: {
    'browserName': 'chrome'
  },

  framework: 'cucumber',

  // Spec patterns are relative to the current working directly when
  // protractor is called.
  specs: [paths.e2e + '/*.feature'],

  cucumberOpts: {
    require: paths.e2e + '/steps/*.js'
  },

  resultJsonOutputFile: 'test-report.json'
};

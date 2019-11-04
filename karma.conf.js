'use strict';

const webpack = require('./webpack/webpack.test');

module.exports = function(config) {

  var configuration = {
    basePath: './',

    files: [
        'src/test/javascript/index.js'
    ],

    preprocessors: {
      'src/test/javascript/index.js': ['webpack']
    },

    singleRun: true,

    autoWatch: false,

    logLevel: 'INFO',

    frameworks: ['jasmine'],

    browsers: ["ChromeHeadless"],

    plugins : [
      'karma-chrome-launcher',
      'karma-coverage',
      'karma-jasmine',
      'karma-webpack'
    ],

    webpack,
    webpackMiddleware: {
      noInfo: true,
      stats: "errors-only"
    },

    coverageReporter: {
      type : 'html',
      dir : 'coverage/'
    },

    reporters: ['progress']

  };

  config.set(configuration);
};

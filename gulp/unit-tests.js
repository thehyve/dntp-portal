/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var del = require('del');
var karma = require('karma');

var pathSrcHtml = [
  path.join(conf.paths.src, 'app/**/*.html')
];

var pathSrcJs = [
  path.join(conf.paths.src, '/**/!(*.spec).js')
];

gulp.task('run-tests', function(done) {
  var reporters = ['progress'];
  var preprocessors = {};

  pathSrcHtml.forEach(function(path) {
    preprocessors[path] = ['ng-html2js'];
  });

  pathSrcJs.forEach(function(path) {
    preprocessors[path] = ['coverage'];
  });
  reporters.push('coverage');

  var localConfig = {
    configFile: path.join(__dirname, '/../karma.conf.js'),
    singleRun: true,
    autoWatch: false,
    reporters: reporters,
    preprocessors: preprocessors
  };

  var server = new karma.Server(localConfig, function(failCount) {
    done(failCount ? new Error("Failed " + failCount + " tests.") : null);
  });
  server.start();
});

gulp.task('test', gulp.series('scripts', 'run-tests'));

gulp.task('clean-tests', function(done) {
  return del([conf.paths.coverage + '/'], done);
});

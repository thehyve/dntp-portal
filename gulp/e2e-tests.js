/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var gulp = require('gulp');

var $ = require('gulp-load-plugins')();

var browserSync = require('browser-sync');

module.exports = function(options) {
  // Downloads the selenium webdriver
  gulp.task('webdriver-update', $.protractor.webdriver_update);

  gulp.task('webdriver-standalone', $.protractor.webdriver_standalone);

  function runProtractor (done) {

    gulp.src(options.e2e + '/**/*.js')
      .pipe($.protractor.protractor({
        configFile: options.e2e + '/protractor.conf.js'
      }))
      .on('error', function (err) {
        // Make sure failed tests cause gulp to exit non-zero
        throw err;
      })
      .on('end', function () {
        // Close browser sync server
        browserSync.exit();
        done();
      });
  }

  gulp.task('protractor', gulp.series('protractor:src'));
  gulp.task('protractor:src', gulp.series('serve:e2e', 'webdriver-update', runProtractor));
  gulp.task('protractor:dist', gulp.series('serve:e2e-dist', 'webdriver-update', runProtractor));
};

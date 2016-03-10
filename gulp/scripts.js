/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var gulp = require('gulp');
var browserSync = require('browser-sync');

var $ = require('gulp-load-plugins')();

module.exports = function(options) {
  gulp.task('scripts', function () {
    return gulp.src(options.src + '/app/**/*.js')
      .pipe($.jshint())
      .pipe($.jshint.reporter('jshint-stylish'))
      .pipe(browserSync.reload({ stream: true }))
      .pipe($.size());
  });
};

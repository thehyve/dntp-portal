/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var eslint = require('gulp-eslint');
var size = require('gulp-size');

gulp.task('scripts', function() {
  return buildScripts();
});

function buildScripts() {
  return gulp.src([
      path.join(conf.paths.src, '/app/**/*.js'),
      path.join(conf.paths.src, '/messages/messages*.js')])
    .pipe(eslint())
    .pipe(eslint.format())
    .pipe(size())
}

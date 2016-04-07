/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var gulp = require('gulp');
var conf = require('./conf');
var htmlmin = require('gulp-htmlmin');
var concat = require('gulp-concat');
var sourcemaps = require('gulp-sourcemaps');
var rev = require('gulp-rev');
var revReplace = require('gulp-rev-replace');
var useref = require('gulp-useref');
var filter = require('gulp-filter');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var csso = require('gulp-csso');
var gzip = require('gulp-gzip');
var del = require('del');

gulp.task('copy-static', ['clean-dist'], function() {
    return gulp.src([conf.paths.src+'/**/*', '!'+conf.paths.src+'/index.html'])
        .pipe(gulp.dest(conf.paths.dist))
        ;
});

gulp.task('js-css-combine-revision', ['copy-static'], function() {
    var jsFilter = filter('**/*.js', {restore: true});
    var cssFilter = filter('**/*.css', {restore: true});

    return gulp.src(conf.paths.src+'/index.html')
        .pipe(sourcemaps.init())
        .pipe(useref())             // Concatenate with gulp-useref
        .pipe(jsFilter)
        .pipe(ngAnnotate())
        .pipe(uglify())             // Minify any javascript sources
        //.pipe(gzip())
        .pipe(rev())                // Rename the concatenated files
        .pipe(jsFilter.restore)
        .pipe(cssFilter)
        .pipe(csso())               // Minify any CSS sources
        //.pipe(gzip())
        .pipe(rev())                // Rename the concatenated files
        .pipe(cssFilter.restore)
        .pipe(sourcemaps.write())
        .pipe(gulp.dest(conf.paths.dist))
        .pipe(rev.manifest())       // Write substitutions to manifest file
        .pipe(gulp.dest(conf.paths.dist))
        ;
});

gulp.task('index', ['js-css-combine-revision'], function() {
    var manifest = gulp.src(conf.paths.dist+'/rev-manifest.json');

    return gulp.src(conf.paths.dist+'/index.html')
        .pipe(revReplace({manifest: manifest}))
        .pipe(gulp.dest(conf.paths.dist))
        ;
});

gulp.task('clean-dist', function (done) {
    return del([conf.paths.dist + '/'], done);
});

gulp.task('package', ['index']);

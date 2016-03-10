/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var gulp = require('gulp');
var htmlmin = require('gulp-htmlmin');
var concat = require('gulp-concat')
var sourcemaps = require('gulp-sourcemaps')
var rev = require('gulp-rev');
var revReplace = require('gulp-rev-replace');
var useref = require('gulp-useref');
var filter = require('gulp-filter');
var ngAnnotate = require('gulp-ng-annotate');
var uglify = require('gulp-uglify');
var csso = require('gulp-csso');
var gzip = require('gulp-gzip');
var del = require('del');

module.exports = function(options) {

    gulp.task('copy-static', ['clean-dist'], function() {
        return gulp.src([options.src+'/**/*', '!'+options.src+'/index.html'])
        .pipe(gulp.dest(options.dist))
        ;
    });

    gulp.task('js-css-combine-revision', ['copy-static'], function() {
        var jsFilter = filter('**/*.js', {restore: true});
        var cssFilter = filter('**/*.css', {restore: true});

        return gulp.src(options.src+'/index.html')
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
            .pipe(gulp.dest(options.dist))
            .pipe(rev.manifest())       // Write substitutions to manifest file
            .pipe(gulp.dest(options.dist))
            ;
    });

    gulp.task('index', ['js-css-combine-revision'], function() {
        var manifest = gulp.src(options.dist+'/rev-manifest.json');

        return gulp.src(options.dist+'/index.html')
            .pipe(revReplace({manifest: manifest}))
            .pipe(gulp.dest(options.dist))
            ;
    });

    gulp.task('clean-dist', function (done) {
        return del([options.dist + '/'], done);
    });

    gulp.task('package', ['index']);

};

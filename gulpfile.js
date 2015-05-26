'use strict';

var gulp = require('gulp');
var gutil = require('gulp-util');
var wrench = require('wrench');

var options = {
  src: 'src/main/resources/static',
  dist: 'dist',
  tmp: '.tmp',
  e2e: 'e2e',
  errorHandler: function(title) {
    return function(err) {
      gutil.log(gutil.colors.red('[' + title + ']'), err.toString());
      this.emit('end');
    };
  },
  wiredep: {
    directory: 'src/main/resources/static/bower_components',
    exclude: [/jquery/, /bootstrap\.js/]
  }
};

// Require all files in the `gulp` directory
wrench.readdirSyncRecursive('./gulp').filter(function(file) {
  return (/\.js$/i).test(file);
}).map(function(file) {
  require('./gulp/' + file)(options);
});

gulp.task('default', ['clean'], function () {
    gulp.start('build');
});

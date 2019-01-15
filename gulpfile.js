'use strict';

var gulp = require('gulp');

require('./gulp/conf');
require('./gulp/scripts');
require('./gulp/styles');
require('./gulp/inject');
require('./gulp/build');
require('./gulp/package');
require('./gulp/watch');
require('./gulp/server');
require('./gulp/unit-tests');
require('./gulp/e2e-tests');

/**
 *  Default task clean temporaries directories and launch the
 *  main optimization build task
 */
gulp.task('default', gulp.series('clean', function () {
    gulp.start('build');
}));

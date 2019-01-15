'use strict';

var gulp = require('gulp');

require('./gulp/conf');
require('./gulp/scripts');
require('./gulp/package');
require('./gulp/unit-tests');

/**
 *  Default task clean temporaries directories and launch the
 *  main packaging task
 */
gulp.task('default', gulp.series('package'));

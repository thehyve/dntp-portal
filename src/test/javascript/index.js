import 'angular';
import 'angular-mocks';

// require all test files using special Webpack feature
// https://webpack.github.io/docs/context.html#require-context
const testsContext = require.context("../../main/webapp/app/", true, /\.spec$/);
testsContext.keys().forEach(testsContext);

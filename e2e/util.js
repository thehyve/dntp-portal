'use strict';

var chai = require('chai');
var chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

module.exports = {
    relativeUrls: {
        'login': 'login',
        'forgot password': 'login/forgot-password',
        'requests': ''
    },
    'chai': chai
}
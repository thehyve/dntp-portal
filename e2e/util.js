'use strict';

var chai = require('chai');
var chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

function newMapping(from, to, action) {
    return { 'from': from, 'to': to, 'action': action };
}

var mappings = [
    newMapping('login', 'forgot password', function() {
        var pages = require('./pages/pages');
        pages.login.forgotPasswordButton.click();
    }),
    newMapping('requests', 'create new request', function() {
        var pages = require('./pages/pages');
        pages.requests.createNewRequest.click();
    })
];

module.exports = {
    baseUrl: 'http://localhost:8092/#/',
    relativeUrls: {
        'login': 'login',
        'forgot password': 'login/forgot-password',
        'requests': '',
        'lab requests': 'lab-requests'
    },
    users: {
        'palga': { username: 'palga@dntp.thehyve.nl', password: 'palga' },
        'requester': { username: 'requester@dntp.thehyve.nl', password: 'requester' },
        'invalid': { username: 'kjaurtyqkuwygf', password: '784yfsda' }
    },
    'mappings': mappings,
    'chai': chai,
    getPage: function(page) {
        var relativeUrl = this.relativeUrls[page];
        if (relativeUrl !== undefined) {
            browser.get(this.baseUrl + this.relativeUrls[page]);
        } else {
            this.fatalError('The page ' + page + ' doesn\'t exist');
        }
    },
    login: function(user) {
        var userObj = this.users[user];
        if (userObj !== undefined) {
            this.getPage('login');
            var pages = require('./pages/pages');
            pages.login.setUsername(userObj.username);
            pages.login.setPassword(userObj.password);
            pages.login.login();
        } else {
            this.fatalError('The user ' + user + ' doesn\'t exist');
        }
    },
    fatalError: function(message) {
        throw "Fatal error: the specification is incorrectly expressed! " + message;
    }
}

'use strict';

var chai = require('chai');
var chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

var rp = require('request-promise');

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
        'scientific council': { username: 'scientific_council@dntp.thehyve.nl', password: 'scientific_council' },
        'lab': { username: 'lab_user@dntp.thehyve.nl', password: 'lab_user' },
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
    logout: function() {
        var rawBaseUrl = 'http://localhost:8092/';

        // We test if we are logged in by trying to GET requests
        return rp({uri: rawBaseUrl + 'requests', resolveWithFullResponse: true})
            .then(function(response) {
                // Click on the logout button of the navbar to logout!
                var pages = require('./pages/pages');
                pages.nav.logout();
            })
            .catch(function() {}); // This will catch a possible 403 error;
    },
    clearRequests: function() {
        // Get a page to have the CSRF token
        this.getPage('login');
        browser.sleep(500);
        
        browser.driver.get('http://localhost:8092/test/clear');
        browser.sleep(500);
    },
    fatalError: function(message) {
        throw 'Fatal error: the specification is incorrectly expressed! ' + message;
    }
}

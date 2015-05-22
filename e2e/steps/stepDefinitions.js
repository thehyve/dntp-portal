'use strict';

var util = require('../util.js');
var expect = util.chai.expect;

module.exports = function() {
    var pages = require('../pages/pages');
    var baseUrl = 'http://localhost:8092/#/';

    this.Given(/^I am on the (.*) page$/, function(pageName, next) {
        var relativeUrl = util.relativeUrls[pageName];
        if (relativeUrl !== undefined) {
            browser.get(baseUrl + relativeUrl);
        } else {
            throw "Fatal error: the specification is incorrectly expressed! "
                + 'The ' + pageName + ' page does not exist!';
        }

        next();
    });

    this.Given(/^I am logged in as the palga user$/, function(next) {
        browser.get(baseUrl);
        pages.login.login();
        next();
    });

    this.When('I log in as the palga user', function(next) {
        pages.login.login();
        next();
    });

    this.When('I log in with invalid credentials', function(next) {
        pages.login.setUsername('invalidUsername');
        pages.login.setPassword('12345678');
        pages.login.login();
        next();
    });

    this.When(/^I click on the "(.*)" button$/, function(name, next) {
        element(by.buttonText(name)).click();
        next();
    });

    this.When('I click on "Forgot Password" from the login page', function(next) {
        pages.login.forgotPasswordButton.click();
        next();
    });

    this.When('I submit any email', function(next) {
        pages.forgotPassword.emailField.sendKeys('asdf@ghi.jk');
        pages.forgotPassword.submitButton.click();
        next();
    });

    this.Then('I should see the application dashboard', function(next) {
        pages.requests.verify(expect);
        pages.requests.createNewRequest.click();
        pages.requests.createRequestForm.cancel.click();
        next();
    });

    this.Then('I should see a log in error', function(next) {
        // Check that an error message is shown
        expect(pages.login.errorMessage).not.equals(undefined);
        next();
    });

    this.Then('I should see a success message', function(next) {
        expect(pages.forgotPassword.successMessage.getText()).to.eventually
            .equal('An email has been sent to asdf@ghi.jk with instructions on how to reset your password');
        next();
    });

    this.Then(/^I should be on the (.*) page$/, function(pageName, next) {
        var relativeUrl = util.relativeUrls[pageName];

        // Check that the pageName exists
        if (relativeUrl === undefined) {
            throw "Fatal error: the specification is incorrectly expressed! "
                + 'The ' + pageName + ' page does not exist!';
        }

        // Check that we are on the associated url
        expect(browser.getCurrentUrl()).to.eventually.equal(baseUrl + relativeUrl).and.notify(next);
    });
};
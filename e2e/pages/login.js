/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
/**
 * This file uses the Page Object pattern to define the main page for tests
 * https://docs.google.com/presentation/d/1B6manhG0zEXkC-H-tPo2vwU06JhL8w9-XCF9oehXzAQ
 */

'use strict';

var LoginPage = function() {
    this.username = element(by.id('username'));
    this.password = element(by.id('password'));
    this.loginButton = element(by.buttonText('Login'));
    this.forgotPasswordButton = element(by.id('forgot-password'));
    this.errorMessage = element(by.css('.alert-danger'));

    this.setUsername = function(username) {
        this.username.clear();
        setText(this.username, username);
    }

    this.setPassword = function(password) {
        setText(this.password, password);
    }

    this.login = function() {
        return this.loginButton.click();
    }
};

// Utility function to set the text in a text field
function setText(element, text) {
    // Ctrl + a to select all and send the rest of the keys
    element.sendKeys(protractor.Key.chord(protractor.Key.CONTROL, 'a'), text);
}

module.exports = new LoginPage();

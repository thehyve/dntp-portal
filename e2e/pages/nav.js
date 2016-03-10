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

// The navigation bar
var NavPage = function() {
    this.logoutButton = element(by.id('nav-logout'));

    this.logout = function() {
        this.logoutButton.click();
    }
};

module.exports = new NavPage();

/**
 * This file uses the Page Object pattern to define the main page for tests
 * https://docs.google.com/presentation/d/1B6manhG0zEXkC-H-tPo2vwU06JhL8w9-XCF9oehXzAQ
 */

'use strict';


var NavPage = function() {
    this.container = element(by.css('.container'));
    this.profileMenu = element(by.id('profile_menu'));
    this.logout = element(by.id('logout'));

    this.verify = function() {
        expect(this.profileMenu);
    }

    this.doLogout = function() {
        this.profileMenu.click();
        this.logout.click();
    }

};

module.exports = new NavPage;

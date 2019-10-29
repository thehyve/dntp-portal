/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

var util = require('../util.js');
var expect = util.chai.expect;
var path = require('path');
var readline = require('readline');

module.exports = function() {
    var pages = require('../pages/pages');
    var baseUrl = util.baseUrl;
    this.setDefaultTimeout(60 * 1000);

    this.Given(/^I am on the (.*) page$/, function(pageName, next) {
        util.getPage(pageName).then(function() {
            next();
        });
    });

    this.Given(/^I am logged in as the (.*) user$/, function(user, next) {
        util.login(user).then(function() {
            browser.sleep(2500);
            next();
        });
    });

    this.Given('I am logged out', function(next) {
        util.logout().then(function() {
            next();
        });
    });

    this.Given('there are no requests', function(next) {
        util.clearRequests().then(function() {
            next();
        });
    });

    this.When(/^I log in as (?:the|an?) (.*) user$/, function(user, next) {
        util.login(user).then(function() {
            next();
        });
    });

    this.When(/^I go from the (.*) page to the (.*) page$/, function(from, to, next) {
        // Check if the mapping is defined
        var mapping;
        for (var i in util.mappings) {
            var m = util.mappings[i];
            if ((m.from === from || m.from ==='*') && m.to === to) {
                mapping = m;
                break;
            }
        }

        if (mapping === undefined) {
            util.fatalError('There is no way to go from `' + from + '` to `' + to + '`');
        }

        mapping.action().then(function() {
            next();
        });
    });

    this.When(/^I go to the '(.+)' page$/, function(to, next) {
        util.getPage(to).then(function() {
            next();
        });
    });

    this.Then(/^the form contains the following data\w*$/, function(fields, next) {
        // fields is a multiline string containing lines of the following format:
        // css_id: new_content
        var regex = /^(.+): (.+)\s*$/;

        var lines = fields.split('\n');

        // Put all promises in the array
        var promises = [];
        for (var i in lines) {
            var matches = lines[i].match(regex);
            var id = matches[1];
            var content = matches[2];
            promises.push(expect(element(by.id(id)).getAttribute('value')).to.eventually.contain(content));
        }

        // Resolve all promises and call next at the end
        Promise.all(promises).then(next, next);
    });

    this.When(/^I fill the form with the following data\w*$/, function(fields, next) {
        // fields is a multiline string containing lines of the following format:
        // css_id: new_content
        var regex = /^(.+): (.+)\s*$/;

        var lines = fields.split('\n');

        // Put all promises in the array
        var promises = [];
        for (var i in lines) {
            var matches = lines[i].match(regex);
            var id = matches[1];
            var content = matches[2];
            promises.push(element(by.id(id)).sendKeys(content));
        }

        // Resolve all promises and call next at the end
        Promise.all(promises).then(function() {
            next();
        });
    });

    this.When(/^I select dropdown option '(.+)'$/, function(type, next) {
        element(by.cssContainingText('option', type)).click().then(function() {
            next();
        });
    });

    this.When(/^I click on the following objects\w*$/, function(fields, next) {
        var lines = fields.split('\n');

        var promises = [];
        for (var id in lines) {
            promises.push(element(by.id(lines[id].trim())).click());
        }
        Promise.all(promises).then(function() {
            next();
        });
    });

    this.When(/^I click on the object with id '(.+)'$/, function(id, next) {
        if (id === 'submit-new-request') { //this is a hack to prevent failure on some screens. should be fixed in a page object.
            // browser.executeScript("angular.element($('#uploadFilePopover')).scope().hidePopover('uploadFilePopover')");
        }
        element(by.id(id)).click().then(next,next);
    });

    this.When(/^I claim the request with title '(.+)'$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        req.claimButton.click().then(function() {
            next();
        });
    });

    this.When(/^I unclaim the request with title '(.+)'$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        req.unclaimButton.click().then(function() {
            next();
        });
    });

    this.When('I claim the current request', function(next) {
        pages.request.claimButton.click().then(function() {
            next();
        });
    });

    this.When(/^I click on the request with title '(.+)'$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        req.title.click().then(function() {
            next();
        });
    });

    this.When(/^I click on the( lab)? request with id '(.+)'$/, function(extra, id, next) {
        element(by.linkText(id.replace('YYYY', new Date().getFullYear()))).click().then(function() {
            next()
        });
    });

    this.When(/^I click on the '(.+)' button$/, function(btnText, next) {
        browser.sleep(500);
        element(by.buttonText(btnText)).click().then(function() {
            next();
        });
    });

    this.When(/^I click on all '(.+)' buttons$/, function(btnText, next) {
        console.log('btnText: ' + btnText);
        element.all(by.buttonText(btnText)).count().then(function(x) {
            console.log('Buttons found: ' + x);
        });
        var promises = [];
        element.all(by.buttonText(btnText)).each(function(btn) {
            console.log('found button!');
            promises.push(btn.click());
        });

        Promise.all(promises).then(function() {
            browser.sleep(500);
            next();
        });
    });

    this.When(/^I upload the file '(.+)' to the element with id '(.+)'$/, function(fileName, id, next) {
        // The file will be located at e2e/files
        var file = path.normalize(__dirname + '/../files/' + fileName);

        // Write the full route to the object
        element(by.id(id)).sendKeys(file).then(function() {
            // Wait for the file to upload
            browser.sleep(1000);
            next();
        });
    });

    this.When(/^I fill the text box with the words '(.+)'$/, function(text, next) {
        element(by.css('[type=text]')).sendKeys(text).then(function() {
            next();
        });
    });

    this.When(/^debug$/, function(next) {
        browser.debugger().then(function() {
            next();
        });
    });

    this.When(/^testing is paused (.*)$/, function(extra, next) {
        var resume = false;

        // Wait until resume becomes true
        browser.driver.wait(function() { return resume; }, 3600 * 1000).then(function() { next(); });

        // Set resume to true the first time a line is read
        var rl = readline.createInterface({ input: process.stdin, output: process.stdout });
        rl.question("Testing is paused " + extra + ", press enter to continue", function() {
            rl.close();
            resume = true;
        });
    });

    this.When(/^I enter the text '(.*)'$/, function(text, next) {
        browser.sleep(500);
        browser.driver.switchTo().activeElement().then(function(element) {
            element.sendKeys(text).then(function() {
                next();
            });
        });
    });

    this.When('I scroll to the bottom of the page', function(next) {
        // Press the End key
        element(by.css('body')).sendKeys(protractor.Key.END).then(function() {
            next();
        });
    });

    // Should work, but doesn't...
    this.When('I select all PA numbers', function(next) {
        var btns = element.all(by.className('select-pa-number'));

        btns.map(function (btn) {
            return btn.click();
        }).then(function (clicks) {
            // This will only be executed when the clicks have been performed
            next();
        });
    });

    this.Then(/^the page should contain the text '(.*)'$/, function(text, next) {
        expect(element(by.css('body')).getAttribute('innerHTML')).to.eventually.contain(text).and.notify(next);
    });

    this.Then(/^the object with class '(.*)' should be present$/, function(cls, next) {
        expect(element(by.css('.'+cls)).isPresent()).to.become(true).and.notify(next);
    });

    this.Then(/^the page should not contain the text '(.*)'$/, function(text, next) {
        expect(element(by.css('body')).getAttribute('innerHTML')).to.eventually.not.contain(text).and.notify(next);
    });

    this.Then(/^I should see an? (.+) message$/, function(msgType, next) {
        var divClass;
        if (msgType === 'success') {
            divClass = 'alert-success';
        } else if (msgType === 'error') {
            divClass = 'alert-danger';
        } else {
            util.fatalError('Message type does not exist: ' + msgType);
        }

        // This point is only reached if the message type exists
        expect(element(by.css("." + divClass)).isPresent()).to.become(true).and.notify(next);
    });

    this.Then(/^I close the print window$/, function(next) {
        browser.actions().sendKeys(protractor.Key.ESCAPE).perform().then(next, next);
    });

    this.Then(/^I should see (\d+) lab requests in the list$/, function(amount, next) {
        amount = parseInt(amount);
        expect(element.all(by.repeater('labRequest in displayedLabRequests track by $index')).count())
            .to.eventually.equal(amount).and.notify(next);
    });

    this.Then(/^I should be on the (.+) page$/, function(pageName, next) {
        var relativeUrl = util.relativeUrls[pageName];

        // Check that the pageName exists
        if (relativeUrl === undefined) {
            util.fatalError('The ' + pageName + ' page does not exist!');
        }

        // Check that we are on the associated url
        expect(browser.getCurrentUrl()).to.eventually.equal(baseUrl + relativeUrl).and.notify(next);
    });

    this.Then(/^request '(.+)' should be in the list of requests$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        expect(req.title.isPresent()).to.eventually.be.true.and.notify(next);
    });

    this.Then(/^request '(.+)' should have status '(.+)'$/, function(reqName, status, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        expect(req.status.getText()).to.eventually.contain(status).and.notify(next);
    });

    this.Then(/^request '(.+)' should have vote '(.+)'$/, function(reqName, vote, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        expect(req.vote.getText()).to.eventually.equal(vote).and.notify(next);
    });

    this.Then(/^request '(.+)' should have Palga advisor '(.+)'$/, function(reqName, advisor, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        advisor.split('\\').forEach(function(element) {
            expect(req.assignee.getText()).to.eventually.contain(element).and.notify(next);
        });
    });

    this.Then(/^the current request should have '(.+)' status$/, function(status, next) {
        expect(element(by.id('requestStatus')).getText()).to.eventually.contain(status).and.notify(next);
    });

    this.Then(/^I should see (\d+) links? with title '(.+)'$/, function(reqAmount, reqName, next) {
        expect(element.all(by.linkText(reqName)).count()).to.eventually.equal(parseInt(reqAmount)).and.notify(next);
    });

    this.When(/^I should see a link to the request with id '(.+)'$/, function(id, next) {
        expect(element(by.linkText(id.replace('YYYY', new Date().getFullYear()))).isPresent())
            .to.eventually.be.true.and.notify(next);
    });

    this.Then('an excerpt should be attached to the request', function(next) {
        expect(pages.request.attachedExcerpt.isPresent()).to.eventually.be.true.and.notify(next);
    });

    this.Then('the scenario should always succeed', function(next) {
        next();
    });

    this.Then(/^the object with id '(.+)' should be ticked$/, function(id, next) {
        expect(element(by.id(id)).getAttribute('class')).to.eventually.contain('glyphicon-check').and.notify(next);
    });

    this.Then(/^the object with id '(.+)' should not be ticked$/, function(id, next) {
        expect(element(by.id(id)).getAttribute('class')).to.eventually.not.contain('glyphicon-check').and.notify(next);
    });

    this.Then(/^the object with id '(.+)' should be enabled$/, function(id, next) {
        expect(element(by.id(id)).getAttribute('disabled')).to.eventually.be.null.and.notify(next);
    });

    this.Then(/^the object with id '(.+)' should be disabled$/, function(id, next) {
        expect(element(by.id(id)).getAttribute('disabled')).to.eventually.contain('true').and.notify(next);
    });
};

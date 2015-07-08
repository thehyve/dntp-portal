'use strict';

var util = require('../util.js');
var expect = util.chai.expect;
var path = require('path');
var readline = require('readline');

module.exports = function() {
    var pages = require('../pages/pages');
    var baseUrl = util.baseUrl;

    this.Given(/^I am on the (.*) page$/, function(pageName, next) {
        util.getPage(pageName);
        next();
    });

    this.Given(/^I am logged in as the (.*) user$/, function(user, next) {
        util.login(user);
        browser.sleep(500);
        next();
    });

    this.Given('I am logged out', function(next) {
        util.logout().then(function() {
            next();
        });
    });

    this.Given('there are no requests', function(next) {
        util.clearRequests();
        next();
    });

    this.When(/^I log in as (?:the|an?) (.*) user$/, function(user, next) {
        util.login(user);
        next();
    });

    this.When(/^I go from the (.*) page to the (.*) page$/, function(from, to, next) {
        // Check if the mapping is defined
        for (var i in util.mappings) {
            var m = util.mappings[i];
            if (m.from === from && m.to === to) {
                m.action();
                next();
                return;
            }
        }

        // At this point we know the mapping doesn't exist!
        util.fatalError('There is no way to go from `' + from + '` to `' + to + '`');
    });

    this.When(/^I go to the '(.+)' page$/, function(to, next) {
        util.getPage(to);
        next();
    });

    this.When(/^I fill the form with the following data\w*$/, function(fields, next) {
        // fields is a multiline string containing lines of the following format:
        // css_id: new_content
        var regex = /^(.+): (.+)\s*$/;

        var lines = fields.split('\n');
        for (var i in lines) {
            var matches = lines[i].match(regex);
            var id = matches[1];
            var content = matches[2];
            element(by.id(id)).sendKeys(content);
        }
        next();
    });

    this.When(/^I click on the following objects\w*$/, function(fields, next) {
        var lines = fields.split('\n');

        for (var id in lines) {
            element(by.id(lines[id].trim())).click();
        }
        next();
    });

    this.When(/^I click on the object with id '(.+)'$/, function(id, next) {
        //browser.actions().mouseMove(element(by.id(id))).click().perform();
        element(by.id(id)).click();
        next();
    });

    this.When(/^I claim the request with title '(.+)'$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        req.claimButton.click();
        next();
    });

    this.When('I claim the current request', function(next) {
        pages.request.claimButton.click();
        next();
    });

    this.When(/^I click on the request with title '(.+)'$/, function(reqName, next) {
        // We assume that we are on the requests page
        var req = pages.requests.getRequestWithTitle(reqName);
        req.title.click();
        next();
    });

    this.When(/^I click on the '(.+)' button$/, function(btnText, next) {
        browser.sleep(500);
        var btn = element(by.buttonText(btnText));
        browser.actions().mouseMove(btn).click().perform();
        next();
    });

    this.When(/^I click on all '(.+)' buttons$/, function(btnText, next) {
        element.all(by.buttonText(btnText)).each(function(btn) {
            browser.actions().mouseMove(btn).click().perform();
        });
        
        browser.sleep(500);
        next();
    });

    this.When(/^there is a pause of (\d+) milliseconds$/, function(milliseconds, next) {
        browser.sleep(parseInt(milliseconds));
        next();
    });

    this.When(/^I upload the file '(.+)' to the element with id '(.+)'$/, function(fileName, id, next) {
        // The file will be located at e2e/files
        var file = path.normalize(__dirname + '/../files/' + fileName);

        // Write the full route to the object
        element(by.id(id)).sendKeys(file);

        // Wait for the file to upload
        browser.sleep(1000);

        next();
    });
    
    this.When('I go to select PA numbers of the current request', function(next) {
        // The id of the request is contained at the end of the url
        var regex = /\/(\d+)$/;
        browser.getCurrentUrl().then(function(url) {
            var id = url.match(regex)[1];
            browser.get('http://localhost:8092/#/request/' + id + '/selection');
            next();
        });
    });
    
    this.When(/^I fill the text box with the words '(.+)'$/, function(text, next) {
        element(by.css('[type=text]')).sendKeys(text);
        next();
    });

    this.When(/^debug$/, function(next) {
        browser.debugger();
        next();
    });

    this.When(/^testing is paused (.*)$/, function(extra, next) {
        var resume = false;
        browser.driver.wait(function() { return resume; }, 3600 * 1000).then(function() { next(); });
        var rl = readline.createInterface({ input: process.stdin, output: process.stdout });
        rl.question("Testing is paused " + extra + ", press enter to continue", function() {
            rl.close();
            resume = true;
        });
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

    this.Then(/^I should see (\d+) links? with title '(.+)'$/, function(reqAmount, reqName, next) {
        expect(element.all(by.linkText(reqName)).count()).to.eventually.equal(reqAmount).and.notify(next);
    });

    this.Then('an excerpt should be attached to the request', function(next) {
        expect(pages.request.attachedExcerpt.isPresent()).to.eventually.be.true.and.notify(next);
    });
};

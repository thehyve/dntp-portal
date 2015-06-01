'use strict';

var util = require('../util.js');
var expect = util.chai.expect;

module.exports = function() {
    var pages = require('../pages/pages');
    var baseUrl = util.baseUrl;

    this.Given(/^I am on the (.*) page$/, function(pageName, next) {
        util.getPage(pageName);
        next();
    });

    this.Given(/^I am logged in as the (.*) user$/, function(user, next) {
        util.login(user);
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
    
    this.When(/^I click on the object with id '(.+)'$/, function(id, next) {
        element(by.id(id)).click();
        next();
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

    this.Then(/^I should be on the (.*) page$/, function(pageName, next) {
        var relativeUrl = util.relativeUrls[pageName];

        // Check that the pageName exists
        if (relativeUrl === undefined) {
            util.fatalError('The ' + pageName + ' page does not exist!');
        }

        // Check that we are on the associated url
        expect(browser.getCurrentUrl()).to.eventually.equal(baseUrl + relativeUrl).and.notify(next);
    });
};

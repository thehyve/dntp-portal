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

var util = require('../util.js');
var expect = util.chai.expect;

var RequestsPage = function() {
    this.createNewRequest = element(by.id('new-request'));

    this.filters = {
        title: element(by.css('[st-search=title]')),
        status: element(by.css('[st-search=status]')),
        requester: element(by.css('[st-search=requesterName]')),
        assignee: element(by.css('[st-search=assigneeName]')),
        search: element(by.id('request-search'))
    };

    this.verify = function() {
        // Verify the filters are empty
        for (var prop in this.filters) {
            expect(this.filters[prop].getText()).to.eventually.equal('');
        }
    };

    this.getRequestWithTitle = function(title) {
        // Return the first element with the given title
        var titleElem = element(by.linkText(title));

        // The locator to the table row of the request
        var rowLocator = titleElem.element(by.xpath('../..'));

        return {
            title: titleElem,
            claimButton: rowLocator.element(by.css('[title=Claim]')),
            status: rowLocator.element(by.css('td.request-status')),
            vote: rowLocator.element(by.css('td.request-vote'))
        };
    };
};

module.exports = new RequestsPage();

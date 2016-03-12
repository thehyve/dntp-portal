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

var RequestPage = function() {
    this.claimButton = element(by.css('[title=Claim]'));
    this.attachedExcerpt = element(by.id('attached-excerpt-list-1'));
};

module.exports = new RequestPage();

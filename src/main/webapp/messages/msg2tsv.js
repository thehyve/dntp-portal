#!/usr/bin/env node
/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
/**
 * msg2tsv.js
 *
 * Writes language definitions to stdout in tab delimited format.
 *
 * Build: npm install
 *
 * Usage: nodejs msg2tsv.js [-all | <lang>]
 *
 * Examples:
 * nodejs msg2tsv.js nl > nl.tsv
 * nodejs msg2tsv.js -all > all.tsv
 */

const _ = require('lodash');

var msg2tsv = function(lang) {
    var messages = require('./messages_'+lang+'.js');
    _.forIn(messages, function(message, i) {
      var output = [i, message];
      console.log(output.join('\t'));
    });
};

var all = ['nl', 'en'];

var msg2tsv_all = function() {
    var messages = {};
    var keys = [];
    _(all).forEach(function(lang) {
        messages[lang] = require('./messages_'+lang+'.js');
        keys = keys.concat(_.keys(messages[lang]));
    });
    keys = _.uniq(_.compact(keys));
    _(keys).forEach(function(key) {
        var output = [key].concat(
            _.map(all, function(lang) {
                return _.get(messages, [lang, key]);
            })
        );
        console.log(output.join('\t'));
    });
};

if (require.main === module) {
    var lang = 'nl';
    if (process.argv.length > 2) {
        lang = process.argv[2];
    }
    console.warn(process.argv[1].split('/').pop());
    if (lang === '-all') {
        console.warn('Writing messages for all languages to stdout.');
        msg2tsv_all();
    } else {
        console.warn('Writing messages for language \'' + lang + '\' to stdout.');
        msg2tsv(lang);
    }
}


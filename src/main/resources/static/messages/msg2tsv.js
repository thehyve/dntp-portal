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
 * Usage: nodejs msg2tsv.js [lang]
 *
 * Example: nodejs msg2tsv.js nl > nl.tsv
 */
/*global console, process, require*/
(function(console, process, require) {
    'use strict';

    var msg2tsv = function(lang){
        var messages = require('./messages_'+lang+'.js');
        for(var i in messages) {
          console.log(i + "\t" + messages[i]);
        }
    }

    if (require.main === module) {
        var lang = 'nl';
        if (process.argv.length > 2) {
            lang = process.argv[2];
        }
        console.warn(process.argv[1].split('/').pop());
        console.warn('Writing messages for language \'' + lang + '\' to stdout.');
        msg2tsv(lang);
    }

})(console, process, require);
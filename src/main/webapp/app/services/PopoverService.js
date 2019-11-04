/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.services')
    .factory('Popover', ['$timeout',
        function ($timeout) {

            var service = {};

            service.showPopover = function(id) {
                jQuery('#'+id).popover('show');
            };

            service.hidePopover = function(id, popoverEnablers) {
                $timeout(function() {
                    var enablersHaveFocus = false;
                    _(popoverEnablers[id]).forEach(function(enabler) {
                        if (jQuery('#'+enabler).is(':focus')) {
                            enablersHaveFocus = true;
                        }
                    });
                    if (!enablersHaveFocus) { jQuery('#'+id).popover('hide'); }
                });
            };

            /**
             * Enables popovers on the event of several elements receiving focus.
             *
             * @param popoverEnablers a map from the element that has the popover
             *        to a list of enabling elements, e.g.,
             *        <code>{ 'labPopover': ['lab', 'lab_label'] }</code>.
             */
            service.enablePopovers = function(popoverEnablers) {
                $timeout(function() {
                _(popoverEnablers).forIn(function(enablers, id) {
                    _(enablers).forEach(function(enabler) {
                        var el = jQuery('#'+enabler);
                        el.focus(function() {
                            service.showPopover(id);
                        });
                        el.blur(function() {
                            service.hidePopover(id, popoverEnablers);
                        });
                    });
                });
                });
            };

            return service;
        }]);

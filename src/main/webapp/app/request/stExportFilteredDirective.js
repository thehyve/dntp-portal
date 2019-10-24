/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.directives')
    .directive('stExportFiltered', function() {

        /**
         * @ngdoc directive
         * @name stExportFiltered
         * @restrict A
         *
         * @description
         * Exports the filtered collection of a smart-table to a scope variable.
         * N.B.: The scope is the direct parent scope, which probably means
         * the smart table controller scope in this case.
         *
         * Example:
         * `<table st-table="displayedCollection" st-safe-src="requests" class="table" st-export-filtered="filteredCollection">`
         */
        return {
            restrict: 'A',
            require: '^stTable',
            /*eslint-disable no-unused-vars*/
            link: function(scope, element, attrs, ctrl){
                scope.$watch(ctrl.getFilteredCollection, function (val) {
                    scope[attrs.stExportFiltered] = val;
                });
            }
            /*eslint-enable no-unused-vars*/
        };
    });

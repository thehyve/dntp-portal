/**
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.services')
    .factory('RequestTypeService', [ '$rootScope',
        function () {

            var service = {
                requestTypes : [
                    {label: 'Numbers only, exploratory request (OZV)', value: 'Numbers only, exploratory request (OZV)'},
                    {label: 'National request (LZV)', value: 'National request (LZV)'},
                    {label: 'Local request', value: 'Local request'},
                    {label: 'Cohort', value: 'Cohort'},
                    {label: 'T-nr list', value: 'T-nr list'},
                    {label: 'T-nr cohort', value: 'T-nr cohort'},
                    {label: 'Intermediary procedure previous request', value: 'Intermediary procedure previous request'},
                    {label: 'Update previous request', value: 'Update previous request'},
                    {label: 'Update previous cohort', value: 'Update previous cohort'}
                ]
            };

            /**
             * Return an ordered list of predefined request types
             * with '(Other)' as the last element
             * @returns Array of ordered list
             */
            service.getRequestTypes = function() {
                var sortedTypes = _.sortBy(this.requestTypes, 'label');
                sortedTypes.unshift({label:'(Please select a request type)', value: null}); // put at beginning
                sortedTypes.push({label:'(Other)', value: ''}); // put at end
                return sortedTypes;
            };

            /**
             * Check if value predefined request type
             * @param value
             * @returns {*}
             */
            service.findPredefined = function (value) {
                return _.find(this.requestTypes, {value:value});
            };

            service.getOther = function () {
                return {label:'(Other)', value: ''};
            };

            return service;
        }]);

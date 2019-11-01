/**
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular) {
    'use strict';

    angular.module('ProcessApp.services')
        .factory('RequestTypeService', [ '$rootScope',
            function ($rootScope) {

                var service = {
                    requestTypes : [
                        {label: 'Exploratory request', value: 'Exploratory request'},
                        {label: 'National request', value: 'National request'},
                        {label: 'Local request', value: 'Local request'},
                        {label: 'Cohort', value: 'Cohort'},
                        {label: 'National T-nr list', value: 'National T-nr list'},
                        {label: 'Local T-nr list', value: 'Local T-nr list'},
                        {label: 'Intermediary procedure previous national request', value: 'Intermediary procedure previous national request'},
                        {label: 'Update previous national request', value: 'Update previous national request'},
                        {label: 'Update previous cohort', value: 'Update previous cohort'}
                    ]
                };

                /**
                 * Return an ordered list of predefined request types
                 * with '(Other)' as the last element
                 * @returns Array of ordered list
                 */
                service.getRequestTypes = function() {
                    _.forEach(this.requestTypes, function(rt) {
                        rt.label = $rootScope.translate(rt.label);
                    });
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
})(console, _, angular);

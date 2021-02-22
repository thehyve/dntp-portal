/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.services')
    .factory('SpecialismService', [ '$rootScope',
        function () {

            var service = {
                specialisms : [
                    {label:'Gastroenterology', value: 'Gastroenterology'},
                    {label:'Gynaecology', value: 'Gynaecology'},
                    {label:'Dermatology', value: 'Dermatology'},
                    {label:'Medical Oncology', value: 'Medical Oncology'},
                    {label:'Internal Medicine', value: 'Internal Medicine'},
                    {label:'Radiology', value: 'Radiology'},
                    {label:'Radiotherapy', value: 'Radiotherapy'},
                    {label:'Haematology', value: 'Haematology'},
                    {label:'Throat-nose-ear', value: 'Throat-nose-ear'},
                    {label:'Surgery', value: 'Surgery'},
                    {label:'Epidemiology', value: 'Epidemiology'},
                    {label:'Primary care', value: 'Primary care'},
                    {label:'Cardiology', value: 'Cardiology'},
                    {label:'Pathology', value: 'Pathology'},
                    {label:'Lung Disease', value: 'Lung Disease'},
                    {label:'Urology', value: 'Urology'},
                    {label:'Neurology', value: 'Neurology'},
                    {label:'Endocrinology', value: 'Endocrinology'}
                ]
            };

            /**
             * Return an ordered list of predefined specialisations
             * with '(Other)' as the last element
             * @returns Array of ordered list
             */
            service.getSpecialisms = function() {
                var sortedSpecs = _.sortBy(this.specialisms, 'label');
                sortedSpecs.unshift({label:'(Please select a specialism)', value: null}); // put at beginning
                sortedSpecs.push({label:'(Other)', value: ''}); // put at end
                return sortedSpecs;
            };

            /**
             * Check if value predefined specialism
             * @param value
             * @returns {*}
             */
            service.findPredefined = function (value) {
                return _.find(this.specialisms, {value:value});
            };

            service.getOther = function () {
                return {label:'(Other)', value: ''};
            };

            return service;
        }]);

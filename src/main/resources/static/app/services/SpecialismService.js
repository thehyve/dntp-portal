/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular) {
    'use strict';

    angular.module('ProcessApp.services')
        .factory('SpecialismService', [ '$rootScope',
            function ($rootScope) {

                var service = {
                    specialisms : [
                        {label:'Maag-darm-lever-ziekten', value: 'Maag-darm-lever-ziekten'},
                        {label:'Gynaecologie', value: 'Gynaecologie'},
                        {label:'Dermatologie', value: 'Dermatologie'},
                        {label:'Medische Oncologie', value: 'Medische Oncologie'},
                        {label:'Interne geneeskunde', value: 'Interne geneeskunde'},
                        {label:'Radiologie', value: 'Radiologie'},
                        {label:'Radiotherapie', value: 'Radiotherapie'},
                        {label:'Chirurgie', value: 'Chirurgie'},
                        {label:'Hematologie', value: 'Hematologie'},
                        {label:'Keel-neus-oor', value: 'Keel-neus-oor'},
                        {label:'Heelkunde', value: 'Heelkunde'},
                        {label:'Epidemiologie', value: 'Epidemiologie'},
                        {label:'Eerstelijnsgeneeskunde', value: 'Eerstelijnsgeneeskunde'},
                        {label:'Cardiologie', value: 'Cardiologie'},
                        {label:'Pathologie', value: 'Pathologie'},
                        {label:'Longziekten', value: 'Longziekten'},
                        {label:'Urologie', value: 'Urologie'},
                        {label:'Neurologie', value: 'Neurologie'},
                        {label:'Endocrinologie', value: 'Endocrinologie'}
                    ]
                };

                /**
                 * Return an ordered list of predefined specialisations
                 * with '(Other)' as the last element
                 * @returns Array of ordered list
                 */
                service.getSpecialismsOld = function() {
                    var specs = angular.copy(this.specialisms);

                    _.remove(specs, function(s) {
                        return s.value == '';
                    });

                    _.forEach(specs, function(s) {
                        s.label = $rootScope.translate(s.label);
                    });

                    var sortedSpecs = _.sortBy(specs, 'label');
                    sortedSpecs.push(this.getOther());
                    return sortedSpecs;
                };

                service.getSpecialisms = function() {
                    var sortedSpecs = _.sortBy(this.specialisms, 'label');
                    sortedSpecs.push({label:'(Other)', value: '---'});
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
})(console, _, angular);

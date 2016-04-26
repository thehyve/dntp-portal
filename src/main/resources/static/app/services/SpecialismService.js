/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular) {
    'use strict';

    angular.module('ProcessApp.services')
        .factory('SpecialismService', [
            function () {

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
                        {label:'Longziekten', value: 'Longziekten'},
                        {label:'Urologie', value: 'Urologie'},
                        {label:'Neurologie', value: 'Neurologie'},
                        {label:'Endocrinologie', value: 'Endocrinologie'},
                        {label:'(Other)', value: ''} // other
                    ]
                };

                /**
                 * Check if value predefiend specialism
                 * @param value
                 * @returns {*}
                 */
                service.findPredefined = function (value) {
                    return _.find(this.specialisms, {value:value});
                };

                service.getOther = function () {
                    return _.last(this.specialisms);
                };

                return service;
            }]);
})(console, _, angular);

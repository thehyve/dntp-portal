/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(angular, _) {

    'use strict';

    angular.module('ProcessApp.directives')
        .directive('specialismComboBox', [
            function () {
                return {
                    restrict: 'E',
                    scope: {
                        specialismTxt : '=specialism',
                        customClass : '@'
                    },
                    templateUrl: 'app/components/specialism/specialism-template.html',
                    link : function (scope, element, attrs) {


                        scope.specialisms = [
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
                        ];


                        /**
                         * Get selected specialism
                         * @returns {string}
                         */
                        scope.selectedSpecialism = (function () {

                            // find if specialism is predefined
                            var _existingSpecialism = _.find(scope.specialisms, {value:scope.specialismTxt});

                            // if not then it is 'Other'
                            if (!_existingSpecialism)  {
                                _existingSpecialism = _.last(scope.specialisms);
                            }

                            // return found obj, otherwise return whatever in specialismTxt
                            return !_.isEmpty(scope.specialismTxt) ? _existingSpecialism :  scope.specialismTxt;
                        })();

                        /**
                         * Update specialism model
                         * @param newSpecialism
                         */
                        scope.updateSpecialismText = function () {
                            scope.specialismTxt = scope.selectedSpecialism.value;
                        };


                    }
                }
            }]);

})(angular, _);

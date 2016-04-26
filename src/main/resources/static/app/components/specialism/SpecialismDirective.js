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
                        modelValue : '=ngModel',
                        customClass : '@'
                    },
                    require: 'ngModel',
                    priority: 2, // increase the priority so validations defined in this directive will be executed first
                    templateUrl: 'app/components/specialism/specialism-template.html',
                    link : function (scope, element, attrs, ctrl) {

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
                            var _existingSpecialism = _.find(scope.specialisms, {value:scope.modelValue});

                            // if not then it is 'Other'
                            if (!_existingSpecialism)  {
                                _existingSpecialism = _.last(scope.specialisms);
                            }

                            // return found obj, otherwise return whatever in modelValue
                            return !_.isEmpty(scope.modelValue) ? _existingSpecialism :  scope.modelValue;
                        })();

                        /**
                         * Update specialism model
                         * @param newSpecialism
                         */
                        scope.updateSpecialismText = function () {
                            scope.modelValue = _.isEmpty(scope.selectedSpecialism) ?
                                '':scope.selectedSpecialism.value;
                        };

                        /**
                         * Override built in 'required' validation
                         * @param modelValue
                         * @returns {boolean}
                         */
                        ctrl.$validators.required = function (modelValue) {
                            if (ctrl.$isEmpty(modelValue)) {
                                return false;
                            } else {
                                return true;
                            }
                        };

                    }
                }
            }]);

})(angular, _);

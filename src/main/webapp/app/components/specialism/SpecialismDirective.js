/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.directives')
    .directive('specialismComboBox', ['SpecialismService',
        function (SpecialismService) {
            return {
                restrict: 'E',
                scope: {
                    modelValue : '=ngModel',
                    maxlength: '=ngMaxlength',
                    required: '=ngRequired',
                    customClass : '@'
                },
                require: 'ngModel',
                priority: 2, // increase the priority so validations defined in this directive will be executed first
                template: require('./specialism-template.html'),
                link : function (scope, element, attrs, ctrl) {

                    scope.specialisms = SpecialismService.getSpecialisms();

                    /**
                     * Get selected specialism
                     * @returns {string}
                     */
                    scope.selectedSpecialism = (function () {

                        // find if specialism is predefined
                        var _existingSpecialism = SpecialismService.findPredefined(scope.modelValue);

                        // if not then it is 'Other'
                        if (!scope.selectedSpecialism)  {
                            _existingSpecialism = SpecialismService.getOther();
                        }

                        // return found obj, otherwise return whatever in modelValue
                        return !_.isEmpty(scope.modelValue) ? _existingSpecialism :  scope.modelValue;
                    })();

                    // Watch value changes especially to handle asynchronous result
                    scope.$watch('modelValue', function (newVal) {
                        if (newVal === undefined || newVal === null) {  // things undefined or null assigned to
                                                                        // first selection
                            scope.selectedSpecialism = {label:'(Please select a specialism)', value: null};
                        } else {
                            // find if specialism is predefined
                            scope.selectedSpecialism  = SpecialismService.findPredefined(newVal);
                            // if not then it is 'Other'
                            if (!scope.selectedSpecialism)  {
                                scope.selectedSpecialism  = SpecialismService.getOther();
                            }
                        }
                    });

                    /**
                     * Update specialism model
                     */
                    scope.updateSpecialismText = function (obj) {
                        scope.modelValue = _.isEmpty(obj) ?
                            '':obj.value;
                    };

                    /**
                     * Override built in 'required' validation
                     * @param modelValue
                     * @returns {boolean}
                     */
                    ctrl.$validators.required = function (modelValue) {
                        return scope.required === false || !ctrl.$isEmpty(modelValue);
                    };

                    /**
                     * Override built in 'maxlength' validation
                     * @param modelValue
                     * @returns {boolean}
                     */
                    ctrl.$validators.maxlength = function (modelValue) {
                        return !modelValue || !scope.maxlength || modelValue.length <= scope.maxlength;
                    };

                }
            }
        }]);

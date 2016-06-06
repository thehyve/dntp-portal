/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(angular, _) {

    'use strict';

    angular.module('ProcessApp.directives')
        .directive('specialismComboBox', ['SpecialismService',
            function (SpecialismService) {
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

                        scope.specialisms = SpecialismService.getSpecialisms();

                        /**
                         * Get selected specialism
                         * @returns {string}
                         */
                        scope.selectedSpecialism = (function () {

                            // find if specialism is predefined
                            var _existingSpecialism = SpecialismService.findPredefined(scope.modelValue);

                            // if not then it is 'Other'
                            if (!scope.selectedSpecialism && !_.isEmpty(_existingSpecialism))  {
                                _existingSpecialism = SpecialismService.getOther();
                            }

                            // return found obj, otherwise return whatever in modelValue
                            return !_.isEmpty(scope.modelValue) ? _existingSpecialism :  scope.modelValue;
                        })();

                        // Watch value changes especially to handle asynchronous result
                        scope.$watch('modelValue', function (newVal) {
                            // find if specialism is predefined
                            scope.selectedSpecialism  = SpecialismService.findPredefined(newVal);

                            // if not then it is 'Other'
                            if (!scope.selectedSpecialism && !_.isEmpty(newVal))  {
                                scope.selectedSpecialism  = SpecialismService.getOther();
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
                            return !ctrl.$isEmpty(modelValue);
                        };

                    }
                }
            }]);

})(angular, _);

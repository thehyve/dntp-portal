/**
 * Copyright (C) 2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.directives')
    .directive('requestTypeComboBox', ['RequestTypeService',
        function (RequestTypeService) {
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
                template: require('./request-type-template.html'),
                link : function (scope, element, attrs, ctrl) {

                    scope.requestTypes = RequestTypeService.getRequestTypes();

                    /**
                     * Get selected request type
                     * @returns {string}
                     */
                    scope.selectedRequestType = (function () {

                        // find if request type is predefined
                        var _existingRequestType = RequestTypeService.findPredefined(scope.modelValue);

                        // if not then it is 'Other'
                        if (!scope.selectedRequestType)  {
                            _existingRequestType = RequestTypeService.getOther();
                        }

                        // return found obj, otherwise return whatever in modelValue
                        return !_.isEmpty(scope.modelValue) ? _existingRequestType :  scope.modelValue;
                    })();

                    // Watch value changes especially to handle asynchronous result
                    scope.$watch('modelValue', function (newVal) {
                        if (newVal === undefined || newVal === null) {  // things undefined or null assigned to
                                                                        // first selection
                            scope.selectedRequestType = {label:'(Please select a request type)', value: null};
                        } else {
                            // find if request type is predefined
                            scope.selectedRequestType  = RequestTypeService.findPredefined(newVal);
                            // if not then it is 'Other'
                            if (!scope.selectedRequestType)  {
                                scope.selectedRequestType  = RequestTypeService.getOther();
                            }
                        }
                    });

                    /**
                     * Update request type model
                     */
                    scope.updateRequestTypeText = function (obj) {
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

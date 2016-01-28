(function(console, angular) {
    'use strict';

    var numericalRegex = /(?=.*[0-9])/, // at least one numerical
        alphabeticalRegex = /(?=.*[a-zA-Z])/, // at least one alphabet
        specialCharsRegex = /(?=.*[?=!*'();:@&=+$,/?#])/, // at least one special chars
        verySpecialCharsRegex = /(?=.*[^a-zA-Z0-9 ])/; // at least one special chars

    // Validate
    var _validatePassword = function(password) {
        if (password === undefined || password === '') {
            return false;
        } else if (password.length < 8) {
            return false;
        } else if (!verySpecialCharsRegex.test(password)) {
            return false;
        } else if (!(numericalRegex.test(password) || alphabeticalRegex.test(password))) {
            return false;
        }
        return true;
    };

    angular.module('ProcessApp.controllers')
        .directive('compareTo', function () {
            return {
                require: 'ngModel',
                scope: {
                    otherModelValue: '=compareTo'
                },
                link: function(scope, element, attributes, ngModel) {
    
                    ngModel.$validators.compareTo = function(modelValue) {
                        return modelValue === scope.otherModelValue;
                    };
    
                    scope.$watch('otherModelValue', function() {
                        ngModel.$validate();
                    });
                }
            };
        })
        .directive('validatePassword', function () {
            return {
                require: 'ngModel',
                scope: {
                    password: '='
                },
                link: function(scope, element, attributes, ngModel) {
                    ngModel.$validators.validatePassword = function(modelValue) {
                        return _validatePassword(modelValue);
                    };
                }
            };
        });

})(console, angular);

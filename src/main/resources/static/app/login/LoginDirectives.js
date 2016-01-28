(function(angular){
    'use strict';

    angular.module('ProcessApp.directives')
        .directive('lowercaseOnly', function(){
            return {
                require: 'ngModel',
                link: function(scope, element, attrs, modelCtrl) {
                    modelCtrl.$parsers.push(function(inputValue) {
                        // this next if is necessary for when using ng-required on your input.
                        // In such cases, when a letter is typed first, this parser will be called
                        // again, and the 2nd time, the value will be undefined
                        if (inputValue === undefined) { return ''; }
                        var transformedInput = (''+inputValue).toLowerCase();
                        if (transformedInput != inputValue) {
                           modelCtrl.$setViewValue(transformedInput);
                           modelCtrl.$render();
                        }
                        return transformedInput;
                    });
                }
            };
        })
        ;
})(angular);

'use strict';

angular.module('ProcessApp.controllers')
    .controller('ResetPasswordController', ['$scope', '$routeParams', '$timeout', 'Restangular', function ($scope, $routeParams, $timeout, Restangular) {
        $scope.submitted = false;
        $scope.done = false;

        $scope.submitForm = function () {

            //var regex = /(?=.*[!*'();:@&=+$,/?#[\]])+[a-zA-Z0-9!*'();:@&=+$,/?#[\]]{8,}/;

            var numericalRegex = /(?=.*[0-9])/, // at least one numerical
             alphabeticalRegex = /(?=.*[a-z])/, // at least one alphabet
             specialCharsRegex = /(?=.*[?=!*'();:@&=+$,/?#])/; // at least one special chars

            // Validate
            if ($scope.password !== $scope.repeatPassword) {
                error('Passwords do not match');
                return;
            } else if ($scope.password === undefined || $scope.password === '') {
                error('Passwords cannot be empty');
                return;
            } else if ($scope.password.length < 8) {
                error('Passwords must be at least 8 characters long');
                return;
            } else if (!specialCharsRegex.test($scope.password)) {
                error("Password must have at least one special chars ?=!*'();:@&=+$,/?#");
                return;
            } else if (specialCharsRegex.test($scope.password)) {
                if (!(numericalRegex.test($scope.password) || alphabeticalRegex.test($scope.password))) {
                    error("Password must contains at least one character or one number");
                }
            }


            $scope.submitted = true;

            // PUT to server (token and new password)
            Restangular.one('password').post('reset', { token: $routeParams.token, password: $scope.password }).then(function () {
                // Notify user
                $scope.submitted = false;
                $scope.done = true;
            }, function restError() {
                // Error, the token isn't valid!
                $scope.error = true;
            });
        };

        function error(msg) {
            $scope.validationError = msg;
            $timeout(function () {
                $scope.validationError = undefined;
            }, 3000);
        }
}]);

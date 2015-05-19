'use strict';

angular.module('ProcessApp.controllers')
    .controller('ResetPasswordController', ['$scope', '$routeParams', '$timeout', 'Restangular', function ($scope, $routeParams, $timeout, Restangular) {
        $scope.submitted = false;
        $scope.done = false;

        $scope.submitForm = function () {
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

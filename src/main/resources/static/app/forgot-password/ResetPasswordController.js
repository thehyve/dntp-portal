'use strict';

angular.module('ProcessApp.controllers')
    .controller('ResetPasswordController', ['$scope', '$routeParams', '$timeout', 'Restangular', function ($scope, $routeParams, $timeout, Restangular) {
        $scope.submitted = false;
        $scope.done = false;

        $scope.submitForm = function () {

            if ($scope.passwordForm.$valid) {
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
            }


        };

        function error(msg) {
            $scope.validationError = msg;
            $timeout(function () {
                $scope.validationError = undefined;
            }, 3000);
        }
}]);

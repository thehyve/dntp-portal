/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, angular) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('PasswordController', ['$scope', '$timeout', 'Restangular', function ($scope, $timeout, Restangular) {
        $scope.form = { oldPassword: '', newPassword: '', repeatNewPassword: '' };
        $scope.submitted = false;
        $scope.done = false;

        // This function will try to validate the data and then make the request
        $scope.submitForm = function () {

            if ($scope.passwordForm.$valid) {
                // Submission
                $scope.submitted = true;

                // POST to server (old and new password)
                Restangular.one('password').post('change', $scope.form).then(function () {
                    // Notify user
                    $scope.submitted = false;
                    $scope.done = true;
                }, function restError() {
                    // The old password is incorrect
                    error('The old password is incorrect');
                    $scope.submitted = false;
                });
            }
        };

        function error(msg) {
            $scope.error = msg;
            $timeout(function () {
                $scope.error = undefined;
            }, 3000);
        }
}]);
})(console, angular);

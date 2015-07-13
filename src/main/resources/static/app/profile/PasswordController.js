'use strict';

angular.module('ProcessApp.controllers')
    .controller('PasswordController', ['$scope', '$timeout', 'Restangular', function ($scope, $timeout, Restangular) {
        $scope.form = { oldPassword: '', newPassword: '', repeatNewPassword: '' };
        $scope.submitted = false;
        $scope.done = false;

        var regex2 = /(?=.*[!*'();:@&=+$,/?#[\]])+[a-zA-Z0-9!*'();:@&=+$,/?#[\]]{8,}/;

        // This function will try to validate the data and then make the request
        $scope.submitForm = function () {
            // Validation
            if ($scope.form.newPassword !== $scope.form.repeatNewPassword) {
                error('Passwords do not match');
                return;
            } else if (regex2.test($scope.form.newPassword) === false) {
                error('Password must be minimum 8 characters long, alphanumeric, and contains ' +
                    'at least one of the following characters !*\'();:@&=+$,/?#[]');
                return;
            } else if ($scope.form.oldPassword === $scope.form.newPassword) {
                error('New password cannot be the same as old password');
                return;
            }

            var numericalRegex = /(?=.*[0-9])/, // at least one numerical
                alphabeticalRegex = /(?=.*[a-z])/, // at least one alphabet
                specialCharsRegex = /(?=.*[?=!*'();:@&=+$,/?#])/; // at least one special chars

            // Validate
            if ($scope.form.newPassword !== $scope.form.repeatNewPassword) {
                error('Passwords do not match');
                return;
            } else if ($scope.form.oldPassword === $scope.form.newPassword) {
                error('New password cannot be the same as old password');
                return;
            } else if ($scope.form.newPassword === undefined || $scope.form.newPassword === '') {
                error('Passwords cannot be empty');
                return;
            } else if ($scope.form.newPassword.length < 8) {
                error('Passwords must be at least 8 characters long');
                return;
            } else if (!specialCharsRegex.test($scope.form.newPassword)) {
                error("Password must have at least one special chars ?=!*'();:@&=+$,/?#");
                return;
            } else if (specialCharsRegex.test($scope.form.newPassword)) {
                if (!(numericalRegex.test($scope.form.newPassword) || alphabeticalRegex.test($scope.form.newPassword))) {
                    error("Password must contains at least one character or one number");
                }
            }

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
        };

        function error(msg) {
            $scope.error = msg;
            $timeout(function () {
                $scope.error = undefined;
            }, 3000);
        }
}]);

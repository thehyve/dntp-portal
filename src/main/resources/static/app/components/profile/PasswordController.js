'use strict';

angular.module('ProcessApp.controllers')
    .controller('PasswordController', ['$scope', '$timeout', 'Restangular', function ($scope, $timeout, Restangular) {
        $scope.form = { oldPassword: '', newPassword: '', repeatNewPassword: '' };
        $scope.submitted = false;
        $scope.done = false;

        // This function will try to validate the data and then make the request
        $scope.submitForm = function () {
            // Validation
            if ($scope.form.newPassword !== $scope.form.repeatNewPassword) {
                error("Passwords do not match");
                return;
            } else if ($scope.form.oldPassword === '' || $scope.form.newPassword === '') {
                error('Passwords cannot be empty');
                return;
            } else if ($scope.form.newPassword.length < 8) {
                error('Passwords must be at least 8 characters long');
                return;
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

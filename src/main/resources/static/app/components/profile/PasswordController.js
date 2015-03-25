'use strict';

angular.module('ProcessApp.controllers')
    .controller('PasswordController', ['$scope', 'Restangular', function ($scope, Restangular) {
        $scope.form = {};
        $scope.submitted = false;
        $scope.done = false;

        // This function will only be called if the form has been validated,
        // because the button will be disabled otherwise.
        $scope.submitForm = function () {
            $scope.submitted = true;

            // POST to server (old and new password)
            Restangular.one('password').post('change', $scope.form).then(function () {
                // Notify user
                $scope.submitted = false;
                $scope.done = true;
            }, function restError() {
                alert('Server error');
            });
        };
}]);

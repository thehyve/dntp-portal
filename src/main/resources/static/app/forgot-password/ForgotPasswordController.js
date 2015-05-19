'use strict';

angular.module('ProcessApp.controllers')
    .controller('ForgotPasswordController', ['$scope', 'Restangular', function ($scope, Restangular) {
        $scope.submitted = false;
        $scope.done = false;
        $scope.error = '';

        // This function will only be called if the form has been validated,
        // because the button will be disabled otherwise.
        $scope.submitForm = function () {
            $scope.submitted = true;
            $scope.error = '';

            // PUT to server (old and new password)
            Restangular.one('password/request-new').customPUT({ email: $scope.email }).then(function () {
                // Notify user
                $scope.submitted = false;
                $scope.done = true;
            }, function restError(response) {
                console.log(JSON.stringify(response));
                if (response.data) {
                    $scope.error = response.data.message;
                }
            });
        };
}]);

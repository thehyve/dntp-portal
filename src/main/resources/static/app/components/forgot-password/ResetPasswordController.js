'use strict';

angular.module('ProcessApp.controllers')
    .controller('ResetPasswordController', ['$scope', '$routeParams', 'Restangular', function ($scope, $routeParams, Restangular) {
        $scope.submitted = false;
        $scope.done = false;
        
        // This function will only be called if the form has been validated,
        // because the button will be disabled otherwise.
        $scope.submitForm = function () {
            $scope.submitted = true;

            // PUT to server (token and new password)
            Restangular.one('password').post('reset', { token: $routeParams.token, password: $scope.password }).then(function () {
                // Notify user
                $scope.submitted = false;
                $scope.done = true;
            }, function restError() {
                alert('Server error');
            });
        };
}]);

'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', 'User', function ($scope, User) {
        $scope.submit = function () {
            if ($scope.registrationForm.$valid) {
                var user = new User(
                    $scope.registrationForm.email,
                    $scope.registrationForm.password,
                    false,
                    null
                );
                user.$register().then(function () {
                    console.log("register success");
                }, function (err) {
                    console.log("register error");
                });
            }
        };
}]);

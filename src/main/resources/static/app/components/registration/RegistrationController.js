'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', function ($scope) {

        $scope.user = {
            firstName : "",
            lastName : "",
            email : "",
            telephone : "",
            institution : "",
            specialism : "",
            isPathologyLabMember : "",
            lab : "",
            password : "",
            repeatPassword : ""
        };

        $scope.submitRequestForm = function () {
            if ($scope.registrationForm.$valid) {
                alert("Valid inputs");
            }
        };


}]);

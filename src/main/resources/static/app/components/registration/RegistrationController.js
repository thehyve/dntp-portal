'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', 'User', 'Restangular', '$rootScope', '$location',
        function ($scope, User, Restangular, $rootScope, $location) {

        $scope.labs = []; // init labs

        // Get lab list
        Restangular.all('public/labs').getList()
            .then(function (labs) {
                $scope.labs = labs;
            });

        $scope.submit = function (user) {
            if ($scope.registrationForm.$valid) {

                user.currentRole = "requester";
                user.username = user.contactData.email;

                $rootScope.registrant = user;

                Restangular.all('register/users').post(user)
                    .then(function (data) {
                        $location.path('/register/success');
                    }, function (response) {
                        // Fixme : change to proper bootstraped alert
                        alert(response.data.message);
                        console.log(response.data.message);
                    });
            }
        };
}]);

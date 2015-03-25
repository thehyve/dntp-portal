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

        $scope.submit = function () {
            if ($scope.registrationForm.$valid) {

                // collect inputs as profile
                var profile = {
                    username:  $scope.user.email,
                    firstName: $scope.user.firstName,
                    lastName: $scope.user.lastName,
                    contactData: {
                        email: $scope.user.email,
                        telephone: $scope.user.email
                    },
                    institute: $scope.user.institute,
                    labId: $scope.user.lab.id,
                    specialism: $scope.user.specialism,
                    currentRole: "requester",
                    isPathologist: $scope.user.isPathologyLabMember,
                    password1: $scope.user.password,
                    password2: $scope.user.repeatPassword
                };

                $rootScope.registrant = profile;

                Restangular.all('register/users').post(profile)
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

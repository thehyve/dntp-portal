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


                var numericalRegex = /(?=.*[0-9])/, // at least one numerical
                    alphabeticalRegex = /(?=.*[a-z])/, // at least one alphabet
                    specialCharsRegex = /(?=.*[?=!*'();:@&=+$,/?#])/; // at least one special chars

                // Validate
                if (user.password1 !== user.password2) {
                    alert('Passwords do not match');
                    return;
                } else if (user.password1 === undefined || user.password1 === '') {
                    alert('Passwords cannot be empty');
                    return;
                } else if (user.password1.length < 8) {
                    alert('Passwords must be at least 8 characters long');
                    return;
                } else if (!specialCharsRegex.test(user.password1)) {
                    alert("Password must have at least one special chars ?=!*'();:@&=+$,/?#");
                    return;
                } else if (specialCharsRegex.test(user.password1)) {
                    if (!(numericalRegex.test(user.password1) || alphabeticalRegex.test(user.password1))) {
                        alert("Password must contains at least one character or one number");
                        return;
                    }
                }                

                user.currentRole = 'requester';
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

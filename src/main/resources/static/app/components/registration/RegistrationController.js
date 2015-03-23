'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', 'User', 'Lab', 'Restangular', function ($scope, User, Lab, Restangular) {

        $scope.labs = []; // init labs

        // Get lab list
        Restangular.all('public/labs').getList()
            .then(function (labs) {
                $scope.labs = labs;
            });

        // -*-*-*-*-*-*-*-*-*-*-*- //
        // START DUMMY  //
        // -*-*-*-*-*-*-*-*-*-*-*- //
        $scope.user = {
            firstName :'Foo',
            lastName : 'Bar',
            email: 'foo@dntp.nl',
            telephone: '090000000',
            institution : '15',
            lab : '12',
            specialism : 'Phd Candidate',
            isPathologyLabMember : true,
            password : '12345678',
            repeatPassword : '12345678'
        };
        // -*-*-*-*-*-*-*-*-*-*-*- //
        // END TOO LAZY TO TYPE    //
        // -*-*-*-*-*-*-*-*-*-*-*- //

        $scope.submit = function () {
            if ($scope.registrationForm.$valid) {
                var user;

                Restangular.one('public/labs', $scope.user.lab).get()
                    .then (function (lab) {

                    console.log("selected lab is .. ", lab);

                    user = new User({
                        email:  $scope.user.email,
                        firstname: $scope.user.firstName,
                        lastname: $scope.user.lastName,
                        telephone: $scope.user.telephone,
                        institution: $scope.user.institution,
                        lab: lab,
                        specialism: $scope.user.specialism,
                        isPathologyLabMember: $scope.user.isPathologyLabMember,
                        password: $scope.user.password,
                        null:null
                    });

                    // TODO register new user with restangular
                    //user.$register(function(result) {
                    //    console.log("success ... ", result);
                    //});
                });




            }
        };
}]);

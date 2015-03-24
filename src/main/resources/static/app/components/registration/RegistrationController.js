'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', 'User', 'Restangular', '$q',
        function ($scope, User, Restangular, $q) {

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
            institution : '14',
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

                var promises = [
                    Restangular.one('public/labs', $scope.user.lab).get(),
                    Restangular.one('public/institutions', $scope.user.institution).get()
                ];

                $q.all(promises).then(function (results) {

                    var lab = results[0],
                        institution = results[1];

                    var user = new User({
                        email:  $scope.user.email,
                        firstname: $scope.user.firstName,
                        lastname: $scope.user.lastName,
                        telephone: $scope.user.telephone,
                        institution: institution,
                        lab: lab,
                        specialism: $scope.user.specialism,
                        isPathologyLabMember: $scope.user.isPathologyLabMember,
                        password: $scope.user.password
                    });

                    console.log("about to register new user");
                    //Restangular.all('register/users').customPOST(user).then(function (data) {
                    //    console.log("user " + data + " has just registered.");
                    //});
                    user.$register(function(result) {
                        console.log("success ... ", result);
                    });
                });


            }
        };
}]);

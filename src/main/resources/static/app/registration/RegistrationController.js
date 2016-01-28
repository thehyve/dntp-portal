(function(console, angular) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', '$rootScope',
                                          'Restangular',
                                          '$location', '$alert', '$timeout',
        function ($scope, $rootScope,
                Restangular,
                $location, $alert, $timeout) {

        $scope.labs = []; // init labs

        // Get lab list
        Restangular.all('public/labs').getList()
            .then(function (labs) {
                $scope.labs = labs;
            });

        $scope.submit = function (user) {
            if ($scope.registrationForm.$valid) {
                user.currentRole = 'requester';
                user.username = user.contactData.email;
                $rootScope.registrant = user;
                Restangular.all('register/users').post(user)
                    .then(function (data) {
                        $location.path('/register/success');
                    }, function (response) {
                        if (response.data) {
                            _error(response.data.message);
                        } else {
                            _error('Error');
                        }
                    });
            }
        };

        var _error = function (msg) {
            $alert({
                title : 'Error',
                content : msg,
                placement : 'top-right',
                type : 'danger',
                show : true,
                duration : 5
            });
        };

}]);
})(console, angular);

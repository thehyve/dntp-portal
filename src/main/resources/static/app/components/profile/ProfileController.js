'use strict';

angular.module('ProcessApp.controllers')
    .controller('ProfileController', ['$scope', 'Restangular', function ($scope, Restangular) {
        $scope.loaded = false;
        $scope.submitted = false;

        withProfile(function (profile) {
            $scope.user = profile;
            $scope.loaded = true;
        });

        // This function will only be called if the form has been validated,
        // because the button will be disabled otherwise.
        $scope.submitForm = function () {
            $scope.submitted = true;

            // PUT to server (only the data that we want)
            Restangular.one('profile').put().then(function () {
                // Reload profile
                withProfile(function (profile) {
                    $scope.user = profile;
                    $scope.submitted = false;
                });
            }, function () {
                alert('error!');
            });
        };

        function withProfile(p) {
            Restangular.one('profile').get().then(p, restError);
        }

        function restError() {
            alert('Server error!');
        }
}]);

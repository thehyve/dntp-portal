'use strict';

angular.module('ProcessApp.controllers')
    .controller('ProfileController', ['$scope', '$timeout', 'Restangular', function ($scope, $timeout, Restangular) {
        $scope.loaded = false;
        $scope.submitted = false;

        Restangular.one('profile').get().then(function (profile) {
            $scope.user = profile;
            Restangular.all('public/labs').getList().then(function (labs) {
                $scope.labs = labs;
                $scope.loaded = true;
            }, restError);
        }, restError);
        
        // This function will only be called if the form has been validated,
        // because the button will be disabled otherwise.
        $scope.submitForm = function () {
            $scope.submitted = true;

            // PUT everything to server
            // We need to use customPUT in order to avoid weird behavior of Restangular
            Restangular.one('profile').customPUT($scope.user).then(function () {
                $scope.submitted = false;
                $scope.success = true;
                $timeout(function () {
                    $scope.success = false;
                }, 3000);
            }, function () {
                alert('PUT error!');
            });
        };

        function restError() {
            alert('Server error!');
        }
}]);

/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(angular) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('ProfileController', ['$scope', '$timeout', 'Restangular', '$log',
          function ($scope, $timeout, Restangular, $log) {
        $scope.loaded = false;
        $scope.submitted = false;

        var restError = function () {
            $log.error('Server error!');
        };

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
              restError();
            });
        };


}]);
})(angular);

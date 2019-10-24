/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.controllers')
    .controller('ActivationController',['$scope', '$routeParams', 'Restangular',
        function ($scope, $routeParams, Restangular) {

            $scope.success = undefined;

            // Check if the activation token is valid
            $scope.init = function() {
                Restangular.one('api/register/users/activate/' + $routeParams.token).get().then(function () {
                    // Notify user
                    $scope.success = true;
                }, function restError() {
                    // Something went wrong. The token is incorrect or outdated
                    $scope.success = false;
                });
            };
        }]);

/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.controllers')
    .controller('HubLabsController', ['$scope', 'Restangular',
        function ($scope, Restangular) {
            'use strict';

            $scope.loaded = false;
            $scope.submitted = false;
            $scope.labs = [];

            var restError = function () {
                console.log('Server error!');
            };

            Restangular.one('api/hublabs').get().then(function (labs) {
                $scope.labs = labs;
                $scope.displayedCollection = [].concat($scope.labs);
            }, restError);
}]);

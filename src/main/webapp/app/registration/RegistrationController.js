/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.controllers')
    .controller('RegistrationController',['$scope', '$rootScope',
                                          'Restangular',
                                          '$location', '$alert',
                                          'Popover',
        function ($scope, $rootScope,
                Restangular,
                $location, $alert,
                Popover) {

        $scope.dataLoading = false;

        $scope.labs = []; // init labs

        // Get lab list
        Restangular.all('api/public/labs').getList()
            .then(function (labs) {
                $scope.labs = labs;
            });

        $scope.submit = function (user) {
            if ($scope.registrationForm.$valid) {
                $scope.dataLoading = true;
                user.currentRole = 'requester';
                user.username = user.contactData.email;
                $rootScope.registrant = user;
                Restangular.all('api/register/users').post(user)
                    .then(function () {
                        $location.path('/register/success');
                        $scope.dataLoading = false;
                    }, function (response) {
                        if (response.data) {
                            _error(response.data.message);
                        } else {
                            _error('Error');
                        }
                        $scope.dataLoading = false;
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

        $scope.popoverEnablers = {
            labPopover: ['lab']
        };

        $scope.enablePopovers = function() {
            Popover.enablePopovers($scope.popoverEnablers);
        };

        angular.element(document).ready(function() {
            jQuery('#firstName').focus();
        });

}]);

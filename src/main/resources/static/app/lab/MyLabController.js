/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('MyLabController', ['$scope', 'Restangular',
        function ($scope, Restangular) {
            'use strict';

            $scope.alerts = [];

            $scope.editlab = {};
            $scope.originLab = {};

            /**
             * From AngularJS v1.5.3, http://angularjs.org
             */
            var EMAIL_REGEXP = /^[a-z0-9!#$%&'*+\/=?^_`{|}~.-]+@[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$/i;
            $scope.validateEmail = function(obj) {
                var email = obj.text;
                if (!email) {
                    return false;
                }
                email = email.trim();
                return email.length > 1 && email.length <= 255 && EMAIL_REGEXP.test(email);
            };


            Restangular.one('lab').get()
                .then(function (myLab) {
                    $scope.editlab = myLab;
                    $scope.editlab.emailAddressData = [].concat($scope.editlab.emailAddresses);
                    $scope.globals.currentUser.lab = myLab;
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err.data.message});
                });

            Restangular.one('lab', 'hubusers').get()
            .then(function (hubUsers) {
                $scope.hubUsers = hubUsers;
            }, function (err) {
                $scope.alerts.push({type: 'danger', msg: err.data.message});
            });

            $scope.update = function () {
                $scope.editlab.emailAddresses = _.map(
                        $scope.editlab.emailAddressData,
                        function(obj) { return obj.text; });
                Restangular.one('lab').customPUT($scope.editlab, '')
                    .then(function (){
                        $scope.alerts.push({type: 'success', msg: 'Your lab information has been successfully updated.' });
                    }, function (response) {
                        $scope.error = $scope.error + response.data.message + '\n';
                        $scope.alerts.push({type: 'danger', msg: response.data.message });
                    });
            };

            $scope.reset = function () {
                $scope.editlab = angular.copy($scope.originLab);
            };

            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };

        }]);

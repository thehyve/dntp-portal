angular.module('ProcessApp.controllers')
    .controller('MyLabController', ['$scope', 'Restangular',
        function ($scope, Restangular) {
            'use strict';

            $scope.alerts = [];

            $scope.editlab = {};
            $scope.originLab = {};


            Restangular.one('lab').get()
                .then(function (myLab) {
                    $scope.editlab = myLab;
                    $scope.globals.currentUser.lab = myLab;
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err.data.message});
                });

            $scope.update = function () {
                Restangular.one('lab').customPUT($scope.editlab, '')
                    .then(function (){
                        $scope.alerts.push({type: 'success', msg: 'Your lab credentials has been successfully updated.' });
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

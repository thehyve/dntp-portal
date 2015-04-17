'use strict';

angular.module('ProcessApp.controllers')
    .controller('MyLabController', ['$scope', 'Restangular',
        function ($scope, Restangular) {

            $scope.alerts = [];

            $scope.editlab = {};
            $scope.originLab = {};

            Restangular.one('lab').get()
                .then(function (myLab) {
                    console.log(myLab);
                    $scope.editlab = myLab;
                    $scope.originLab = myLab;
                });

            $scope.update = function () {

                $scope.editlab.save()
                    .then(function (){
                        $scope.alerts.push({type: 'success', msg: 'Your lab credentials has been successfully updated.' });
                    }, function (e) {
                        $scope.error = $scope.error + response.data.message + "\n";
                        $scope.alerts.push({type: 'danger', msg: response.data.message });
                    });
            };

            $scope.reset = function () {
                console.log("about to reset");
                $scope.editlab = angular.copy($scope.originLab);
            }

            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };

        }]);

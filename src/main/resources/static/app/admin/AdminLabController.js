'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminLabController',['$scope', '$modal', 'Lab',
        function ($scope, $modal, Lab) {

            $scope.error = "";
            $scope.accessDenied = false;
            $scope.visibility = {};

            Lab.query(function(response) {
                $scope.labs = response ? response : [];
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
                if (response.data.status == 302 || response.data.status == 403) {
                    $scope.accessDenied = true;
                }
            });

            $scope.add = function() {
                $scope.edit(new Lab());
            };

            $scope.update = function(labdata) {
                if (labdata.id > 0) {
                    labdata.$update(function(result) {
                        $scope.editLabModal.hide();
                    }, function(response) {
                        $scope.error = $scope.error + response.data.message + "\n";
                    });
                } else {
                    var lab = new Lab(labdata);
                    lab.$save(function(result) {
                        $scope.editLabModal.hide();
                        $scope.labs.unshift(result);
                    }, function(response) {
                        $scope.error = response.data.message + "\n";
                    });
                }
            };

            $scope.activate = function(lab) {
                lab.$activate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                });
            };

            $scope.deactivate = function(lab) {
                lab.$deactivate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                });
            };

            $scope.toggleVisibility = function(lab) {
                if (!(lab.id in $scope.visibility)) {
                    $scope.visibility[lab.id] = false;
                }
                $scope.visibility[lab.id] = !$scope.visibility[lab.id];
            };

            $scope.remove = function(lab) {
                lab.$remove(function() {
                    $scope.labs.splice($scope.labs.indexOf(lab), 1);
                }, function(response) {
                    $scope.error = response.statusText;
                });
            };

            $scope.edit = function(lb) {
                $scope.editlab = lb;
                $scope.editLabModal = $modal({scope: $scope, template: '/app/components/admin/editlab.html'});
            };
        }]);

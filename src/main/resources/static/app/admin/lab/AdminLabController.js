'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminLabController',['$rootScope', '$scope', '$location', '$modal', 'Lab',
        function ($rootScope, $scope, $location, $modal, Lab) {

            $rootScope.redirectUrl = $location.path();
    
            $scope.login = function () {
                $location.path('/login');
            };
    
            if (!$rootScope.globals.currentUser) {
                $scope.login();
            }
        
            $scope.error = '';
            $scope.accessDenied = false;
            $scope.visibility = {};

            Lab.query(function(response) {
                $scope.labs = response ? response : [];
            }, function(err) {
                if (err.status === 403) {
                    $rootScope.errormessage = err.data.message;
                    $scope.login();
                    return;
                }
                $scope.error = err.data.message;
            });

            $scope.add = function() {
                $scope.edit(new Lab());
            };

            $scope.update = function(labdata) {
                $scope.dataLoading = true;
                if (labdata.id > 0) {
                    labdata.$update(function(result) {
                        $scope.editLabModal.hide();
                        $scope.dataLoading = false;
                    }, function(response) {
                        $scope.error = $scope.error + response.data.message + '\n';
                        $scope.dataLoading = false;
                    });
                } else {
                    var lab = new Lab(labdata);
                    lab.$save(function(result) {
                        $scope.editLabModal.hide();
                        $scope.labs.unshift(result);
                        $scope.dataLoading = false;
                    }, function(response) {
                        $scope.error = response.data.message + '\n';
                        $scope.dataLoading = false;
                    });
                }
            };

            $scope.activate = function(lab) {
                $scope.dataLoading = true;
                lab.$activate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                    $scope.dataLoading = false;
                });
            };

            $scope.deactivate = function(lab) {
                $scope.dataLoading = true;
                lab.$deactivate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                    $scope.dataLoading = false;
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
                $scope.editLabModal = $modal({scope: $scope, template: '/app/admin/lab/editlab.html'});
            };
        }]);

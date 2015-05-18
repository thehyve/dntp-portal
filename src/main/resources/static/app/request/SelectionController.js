'use strict';

angular.module('ProcessApp.controllers')
    .controller('SelectionController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'ExcerptEntry',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, ExcerptEntry) {

            $scope.updateSelection = function(request, excerpt, selected) {
                var entry = new ExcerptEntry(excerpt);
                entry.selected = selected;
                entry.processInstanceId = request.processInstanceId;
                entry.$update(function(result) {
                    //$scope.request.excerptListSelection[excerpt.sequenceNumber] = result;
                    console.log("Selection updated: " + result);
                    request.excerptList = result;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + "\n";
                });
            };
            
            $scope.selectExcerpt = function(request, excerpt) {
                console.log("Select excerpt: " + excerpt.id + " for request " + request.processInstanceId);
                $scope.updateSelection(request, excerpt, true);
            }
            
            $scope.deselectExcerpt = function(request, excerpt) {
                console.log("Select excerpt: " + excerpt.id + " for request " + request.processInstanceId);
                $scope.updateSelection(request, excerpt, false);
            }
            
            $scope.submitExcerptSelection = function(request) {
                request.$submitExcerptSelection(function(result) {
                    console.log("Selection submitted: " + result);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + "\n";
                });
            }

}]);

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
            
            $scope.currentIndex = 0;
            
            $scope.relevantFields = ["Geslacht_Afkorting", "GebDat"]
            $scope.relevantIndexes = []
            if ($scope.request) {
                for (var field in $scope.relevantFields) {
                    var index = $scope.request.excerptList.columnNames.indexOf($scope.relevantFields[field]);
                    console.log("field "+field+": index = "+index);
                    $scope.relevantIndexes.push(index);
                }
            }
            
            $scope.toggleExcerpt = function(request, excerpt) {
                console.log("Toggle excerpt: " + excerpt.id + " for request " + request.processInstanceId);
                $scope.updateSelection(request, excerpt, !excerpt.selected);
            }
            
            $scope.selectExcerpt = function(request, excerpt) {
                console.log("Select excerpt: " + excerpt.id + " for request " + request.processInstanceId);
                $scope.updateSelection(request, excerpt, true);
            }
            
            $scope.deselectExcerpt = function(request, excerpt) {
                console.log("Deselect excerpt: " + excerpt.id + " for request " + request.processInstanceId);
                $scope.updateSelection(request, excerpt, false);
            }
            
            $scope.submitExcerptSelection = function(request) {
                bootbox.prompt({
                    title: "Are you sure you want to submit the selection?\n<br>"
                    +"You may enter a remark:",
                    callback: function(result) {
                        if (result) {
                            request.excerptListRemark = result;
                            request.$submitExcerptSelection(function(result) {
                                console.log("Selection submitted: " + result);
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + "\n";
                            });
                        }
                    }
                });
            }
            
            $scope.scrollToCurrent = function() {
                var elementId = '#excerpt_'+$scope.request.excerptList.entries[$scope.currentIndex].id;
                if ($(elementId + ":not(:visible)")) {
                    console.log("scroll to: " + elementId);
                    $('html, body').animate({
                        scrollTop: $(elementId).offset().top - 100
                    }, 200);
                }
            }
            
            $(document).keydown(function(e){
                if(e.which == 40) { // down
                    console.log("down: " + $scope.request.excerptList.entries.length);
                    if ($scope.currentIndex < $scope.request.excerptList.entries.length - 1) {
                        $scope.$apply(function() {
                            $scope.currentIndex++;
                        });
                        $('#excerpt_'+$scope.request.excerptList.entries[$scope.currentIndex].id).focus();
                    }
                    $scope.scrollToCurrent();
                    return false; // stops the page from scrolling
                }
                if(e.which == 38) { // up
                    console.log("up");
                    if ($scope.currentIndex > 0) {
                        $scope.$apply(function() {
                            $scope.currentIndex--;
                        });
                    }
                    $scope.scrollToCurrent();
                    return false; // stops the page from scrolling
                }
                if(e.which == 32) { // space
                    console.log("toggle");
                    $scope.$apply(function() {
                        $scope.toggleExcerpt($scope.request, $scope.request.excerptList.entries[$scope.currentIndex]);
                        /*if ($scope.currentIndex < $scope.request.excerptList.entries.length - 1) {
                            $scope.currentIndex++;
                        }*/
                    });
                    $scope.scrollToCurrent();
                    return false; // stops the page from scrolling
                }
            });

}]);

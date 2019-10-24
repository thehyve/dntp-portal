/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';
import bootbox from 'bootbox';

angular.module('ProcessApp.controllers')
    .controller('SelectionController', [
        '$rootScope', '$scope',
        '$modal', '$alert',
        '$location', '$route',
        '$q',
        'Request', 'ExcerptList', 'ExcerptEntry',

        function ($rootScope, $scope,
                  $modal, $alert,
                  $location, $route,
                  $q,
                  Request, ExcerptList, ExcerptEntry) {

            $scope.excerptLabel = $rootScope.translate('Excerpt');

            $scope.init = function() {
                $scope.getExcerptList().then(function() {
                    //console.log('Excerpt list loaded.');

                    $scope.currentIndex = 0;

                    $scope.relevantFields = [
                                             'PALGAPatiëntnr',
                                             'PALGAExcerptnr',
                                             'PALGAExcerptid',
                                             'Jaar onderzoek',
                                             'Conclusie'
                                             ];
                    $scope.relevantIndexes = [];
                    if ($scope.request && $scope.request.excerptList
                            && $scope.request.excerptList.entries.length < 1000) {
                        for (var field in $scope.relevantFields) {
                            var index = $scope.request.excerptList.columnNames
                                .indexOf($scope.relevantFields[field]);
                            //console.log('field '+field+': index = '+index);
                            $scope.relevantIndexes.push(index);
                        }
                    }

                    $scope.enableSpaceSelects();
                });
            };

            $scope.getExcerptList = function() {
                return $q(function(resolve, reject) {
                    ExcerptList.get({
                        processInstanceId: $scope.request.processInstanceId
                    }, function(excerptList) {
                        $scope.request.excerptList = excerptList;
                        resolve(excerptList);
                    }, function(err) {
                        reject(err);
                    });
                });
            };

            $scope.updateSelection = function(request, excerpt, selected) {
                var entry = new ExcerptEntry(excerpt);
                entry.selected = selected;
                entry.processInstanceId = request.processInstanceId;
                entry.$update(function(result) {
                    var index = request.excerptList.entries.indexOf(excerpt);
                    //console.log('selection updated at index ' + index);
                    request.excerptList.entries[index] = result;
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };

            $scope.xls2html = function(text) {
                if (text) {
                    return text.replace(/_x000D_/g, '<br />\n');
                } else {
                    return "";
                }
            };

            $scope.toggleExcerpt = function(request, excerpt) {
                //console.log('Toggle excerpt: ' + excerpt.id + ' for request ' + request.processInstanceId);
                $scope.updateSelection(request, excerpt, !excerpt.selected);
            };

            $scope.selectExcerpt = function(request, excerpt) {
                //console.log('Select excerpt: ' + excerpt.id + ' for request ' + request.processInstanceId);
                $scope.updateSelection(request, excerpt, true);
            };

            $scope.deselectExcerpt = function(request, excerpt) {
                //console.log('Deselect excerpt: ' + excerpt.id + ' for request ' + request.processInstanceId);
                $scope.updateSelection(request, excerpt, false);
            };

            $scope.selectAllExcerpts = function(request) {
                request.$selectAll(function(result) {
                    $scope.request = result;
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };

            $scope.submitExcerptSelection = function(request) {
                $scope.disableSpaceSelects();
                bootbox.confirm($rootScope.translate(
                            'Are you sure you want to submit the selection?'),
                    function(result) {
                        if (result) {
                            request.$submitExcerptSelection(function() {
                                var content = $rootScope.translate('Successfully submitted selection.');
                                $alert({
                                    title : $rootScope.translate('Excerpt selection'),
                                    content : content,
                                    placement : 'top-right',
                                    type : 'success',
                                    show : true,
                                    duration : 5
                                });
                                $location.path('/');
                                _.defer(function(){
                                    $scope.$apply();
                                });
                            }, function(response) {
                                $rootScope.logErrorResponse(response);
                            });
                        } else {
                            var content = $rootScope.translate('No selection submitted.');
                            $alert({
                                title : $rootScope.translate('Excerpt selection'),
                                content : content,
                                placement : 'top-right',
                                type : 'danger',
                                show : true,
                                duration : 5
                            });
                            $scope.enableSpaceSelects();
                        }
                    }
                );
            };

            $scope.scrollToCurrent = function() {
                var elementId = '#excerpt_'+$scope.request.excerptList.entries[$scope.currentIndex].id;
                if (jQuery(elementId + ':not(:visible)')) {
                    //console.log('scroll to: ' + elementId);
                    var element = jQuery(elementId);
                    if (element) {
                        jQuery('html, body').animate({
                            scrollTop: element.offset().top - 200
                        }, 200);
                    }
                }
            };

            $scope.enableSpaceSelects = function() {
                jQuery(document).off('keydown.selection');
                jQuery(document).on('keydown.selection', function(e){
                    if (!$route.current.originalPath.endsWith('/selection')) {
                        $scope.disableSpaceSelects();
                        return;
                    }
                    if (e.which === 40) { // down
                        //console.log('down: ' + $scope.request.excerptList.entries.length);
                        if ($scope.currentIndex < $scope.request.excerptList.entries.length - 1) {
                            $scope.$apply(function() {
                                $scope.currentIndex++;
                            });
                            jQuery('#excerpt_'+$scope.request.excerptList.entries[$scope.currentIndex].id).focus();
                        }
                        $scope.scrollToCurrent();
                        return false; // stops the page from scrolling
                    }
                    if (e.which === 38) { // up
                        //console.log('up');
                        if ($scope.currentIndex > 0) {
                            $scope.$apply(function() {
                                $scope.currentIndex--;
                            });
                        }
                        $scope.scrollToCurrent();
                        return false; // stops the page from scrolling
                    }
                    if (e.which === 32) { // space
                        //console.log('toggle');
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
            };

            $scope.disableSpaceSelects = function() {
                jQuery(document).off('keydown.selection');
            };

            $scope.$on("$destroy", function(){
                $scope.disableSpaceSelects();
            });

}]);

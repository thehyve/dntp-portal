/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.directives')
    .directive('labRequestsSidebar', ['LabRequest', 'LabRequestFilter',
            function(LabRequest, LabRequestFilter) {

        return {
            restrict: 'E',
            scope: {
                allLabRequests: '=',
                activeSidebar: '='
            },
            template: require('./lab-requests-sidebar-template.html'),
            link : function ($scope) {
                var userId = $scope.$root.currentUserId;
                $scope.statusesForRole = LabRequest.statuses;

                $scope.isLabUser = $scope.$root.isLabUser;
                $scope.isHubUser = $scope.$root.isHubUser;

                $scope.changeFilterStatus = () => {
                    var cur_filter = JSON.parse(localStorage.getItem('labrequests'));
                    if ('search' in cur_filter){
                        if('predicateObject' in cur_filter['search']){
                            if ('status' in cur_filter['search']['predicateObject']){
                                delete cur_filter['search']['predicateObject']['status']
                            }
                        }
                    }
                    localStorage.setItem('labrequests', JSON.stringify(cur_filter));
                };

                /*eslint-disable no-unused-vars*/
                $scope.$watch('allLabRequests', function(newValue, oldValue) {
                    if (newValue) {
                        $scope.unclaimedReqs = LabRequestFilter.selectUnclaimed(newValue);
                        $scope.claimedReqs = LabRequestFilter.selectClaimed(userId)(newValue);
                        $scope.requestsHubAssistanceRequested = LabRequestFilter.selectHubAssistanceRequested(newValue);
                        $scope.requestsByStatus = {};
                        _($scope.statusesForRole).forEach(function(status) {
                            $scope.requestsByStatus[status] = LabRequestFilter.selectByStatus(status)(newValue);
                        });
                    }
                });
                /*eslint-enable no-unused-vars*/
            }
        };
    }]);

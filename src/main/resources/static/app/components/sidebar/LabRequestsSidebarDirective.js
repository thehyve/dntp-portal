/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('labRequestsSidebar', ['LabRequest', 'LabRequestFilter',
            function(LabRequest, LabRequestFilter) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                allLabRequests: '=',
                activeSidebar: '='
            },
            templateUrl: 'app/components/sidebar/lab-requests-sidebar-template.html',
            link : function ($scope) {
                var userId = $scope.$root.currentUserId;
                $scope.statusesForRole = LabRequest.statuses;

                $scope.isLabUser = $scope.$root.isLabUser;
                $scope.isHubUser = $scope.$root.isHubUser;

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

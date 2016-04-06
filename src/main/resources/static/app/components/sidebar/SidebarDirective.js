/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('dntpSidebar', ['Request', 'RequestFilter',
            function(Request, RequestFilter) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                allRequests: '=',
                activeSidebar: '='
            },
            templateUrl: 'app/components/sidebar/sidebar-template.html',
            link : function ($scope) {
                var userId = $scope.$root.currentUserId;
                $scope.statusesForRole = Request.getStatusesForRole($scope.$root.currentRole);

                $scope.isPalga = $scope.$root.isPalga;
                $scope.isScientificCouncil = $scope.$root.isScientificCouncil;

                $scope.$watch('allRequests', function(newValue, oldValue) {
                    if (newValue) {
                        $scope.unclaimedReqs = RequestFilter.selectUnclaimed(newValue);
                        $scope.claimedReqs = RequestFilter.selectClaimed(userId)(newValue);
                        $scope.suspendedReqs = RequestFilter.selectSuspended(newValue);
                        $scope.requestsByStatus = {};
                        _($scope.statusesForRole).forEach(function(status) {
                            $scope.requestsByStatus[status] = RequestFilter.selectByStatus(status)(newValue);
                        });
                        $scope.reqsVoted = RequestFilter.selectVoted(newValue);
                        $scope.reqsNotVoted = RequestFilter.selectNotVoted(newValue);
                    }
                });
            }
        };
    }]);

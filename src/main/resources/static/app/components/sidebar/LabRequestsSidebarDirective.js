/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('labRequestsSidebar', ['LabRequest', function(LabRequest) {
        'use strict';

        var _getUnclaimed = function (labrequests) {
            return _.chain(labrequests)
                .filter(_.matches({assignee: null}))
                .value();
        };

        var _getClaimed = function (labrequests, userId) {
            return _.chain(labrequests)
                .filter(_.matches({assignee: userId}))
                .value();
        };

        var _getRequestsByStatus = function (labrequests, statuses) {
            var result = {};
            _(statuses).forEach(function(status) {
                result[status] = _.chain(labrequests)
                    .filter(_.matches({status: status}))
                    .value();
            });
            return result;
        };

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

                $scope.$watch('allLabRequests', function(newValue, oldValue) {
                    if (newValue) {
                        $scope.unclaimedReqs = _getUnclaimed(newValue);
                        $scope.claimedReqs = _getClaimed(newValue, userId);
                        $scope.requestsByStatus = _getRequestsByStatus(newValue, $scope.statusesForRole);
                    }
                });
            }
        };
    }]);

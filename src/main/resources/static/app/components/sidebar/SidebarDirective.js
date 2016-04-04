/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('dntpSidebar', ['Request', function(Request) {
        'use strict';

        var _isSuspended = _.matches({reviewStatus: 'SUSPENDED'});
        var _isNotSuspended = _.negate(_isSuspended);

        var _getUnclaimed = function (requests) {
            return _.chain(requests)
                .filter(_.matches({assignee: null}))
                .filter(_isNotSuspended)
                .value();
        };

        var _getClaimed = function (requests, userId) {
            return _.chain(requests)
                .filter(_.matches({assignee: userId}))
                .filter(_isNotSuspended)
                .value();
        };

        var _getSuspended = function (requests) {
            return _.filter(requests, _isSuspended);
        };

        var _getRequestsByStatus = function (requests, statuses) {
            var result = {};
            _(statuses).forEach(function(status) {
                result[status] = _.chain(requests)
                    .filter(_.matches({status: status}))
                    .filter(_isNotSuspended)
                    .value();
            });
            return result;
        };

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

                $scope.$watch('allRequests', function(newValue, oldValue) {
                    if (newValue) {
                        $scope.unclaimedReqs = _getUnclaimed(newValue);
                        $scope.claimedReqs = _getClaimed(newValue, userId);
                        $scope.suspendedReqs = _getSuspended(newValue);
                        $scope.requestsByStatus = _getRequestsByStatus(newValue, $scope.statusesForRole);
                    }
                });
            }
        };
    }]);

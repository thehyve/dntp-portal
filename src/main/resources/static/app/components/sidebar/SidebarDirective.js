/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives', [])
    .directive('dntpSidebar', function() {
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

        return {
            restrict: 'E',
            scope: {
                allRequests: '=',
                activeSidebar: '='
            },
            templateUrl: 'app/components/sidebar/sidebar-template.html',
            link : function ($scope) {
                var userId = $scope.$root.globals.currentUser.userid;
                $scope.userRoles = $scope.$root.globals.currentUser.roles;

                $scope.isPalga = function() {
                    return $scope.userRoles.indexOf('palga') !== -1;
                };

                $scope.$watch('allRequests', function(newValue, oldValue) {
                    if (newValue) {
                        $scope.unclaimedReqs = _getUnclaimed(newValue);
                        $scope.claimedReqs = _getClaimed(newValue, userId);
                        $scope.suspendedReqs = _getSuspended(newValue);
                    }
                });
            }
        };
    });

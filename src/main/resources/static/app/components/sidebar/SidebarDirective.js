'use strict';

angular.module('ProcessApp.directives', [])
    .directive('dntpSidebar', function() {

        var _getUnclaimed = function (requests) {
            return _.where( requests, {assignee:null});
        };

        var _getClaimed = function (requests, userId) {
            return _.where( requests, {assignee:userId});
        };

        var _getSuspended = function (requests) {
            return _.where( requests, {reviewStatus:'SUSPENDED'});
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

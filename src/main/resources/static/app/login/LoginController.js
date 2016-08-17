/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
'use strict';

angular.module('ProcessApp.controllers')
    .controller('LoginController',['$scope', '$http', '$rootScope', '$location', '$route', '$cookies', '$log', 'LoginService',
        function ($scope, $http, $rootScope, $location, $route, $cookies, $log, LoginService) {

            $scope.dataLoading = false;

            $scope.credentials = {
                username: $cookies.get('username')
            };

            $scope.login = function(closemodal) {
                $scope.dataLoading = true;
                LoginService.login($scope.credentials).then(function() {
                    // successful login
                    $scope.dataLoading = false;
                    if (closemodal) {
                        LoginService.hideLogin();
                        if ($scope.reloadPageAfterLogin) {
                            $route.reload();
                        }
                    } else {
                        var redirectUrl = $rootScope.redirectUrl;
                        if (!redirectUrl) {
                            if ($rootScope.isLabUser() || $rootScope.isHubUser()) {
                                redirectUrl = '/lab-requests';
                            } else {
                                redirectUrl = '/';
                            }
                        }
                        $location.path(redirectUrl);
                    }
                }, function() {
                    // login failed. message in $rootScope.errormessage.
                    $scope.dataLoading = false;
                });
            };

            $scope.cancelByEscKey = function (key) {
                //console.log('In cancelByEscKey');
                if (key.keyCode === 27) {
                    //console.log('Escape key');
                    LoginService.hideLogin();
                }
            };

            angular.element(document).ready(function() {
                jQuery('#username').focus();
              });

        }]);

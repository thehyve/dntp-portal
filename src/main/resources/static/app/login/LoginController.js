/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('LoginController',['$scope', '$http', '$rootScope', '$location', '$cookies',
        function ($scope, $http, $rootScope, $location, $cookies) {
            'use strict';

            var _serialiseRoles = function(roles) {
                if (!jQuery.isArray(roles)) { 
                    return ''; 
                } else {
                    return roles.join(',');
                }
            };

            var _storeUserdata = function(userdata) {
                $cookies.put('userid', userdata.userid);
                $cookies.put('username', userdata.username);
                $cookies.put('roles', _serialiseRoles(userdata.roles));
            };

            var authenticate = function(callback) {
                $http.get('user').success(function(data) {
                    // console.log('Login succes: ' + JSON.stringify(data));
                    if (data.username) {
                        var userdata = {
                            userid: ''+data.id,
                            username: data.username,
                            roles: [],
                            features: [],
                            lab: null
                        };
                        if (data.roles) {
                            for(var i in data.roles) {
                                userdata.roles.push(data.roles[i].name);
                            }
                        }
                        _storeUserdata(userdata);
                        $rootScope.updateUserData(userdata);
                    } else {
                        $rootScope.authenticated = false;
                    }
                    if (callback) { callback(); }
                }).error(function() {
                    $rootScope.authenticated = false;
                    if (callback) { callback(); }
                });
            };

            $scope.credentials = {
                username: $cookies.get('username')
            };

            var _refreshCookie = function() {
                return $http.get('/ping');
            };

            $scope.login = function() {
                $scope.dataLoading = true;
                _refreshCookie()
                .then(function(result) {
                    $http.post('login', jQuery.param($scope.credentials), {
                        headers : {
                            'content-type' : 'application/x-www-form-urlencoded'
                        }
                    }).success(function(data) {
                        authenticate(function() {
                            $scope.dataLoading = false;
                            if ($rootScope.authenticated) {
                                $rootScope.error = false;
                                var redirectUrl = $rootScope.redirectUrl;
                                if (!redirectUrl) {
                                    if ($rootScope.isLabUser() || $rootScope.isHubUser()) {
                                        redirectUrl = '/lab-requests';
                                    } else {
                                        redirectUrl = '/';
                                    }
                                }
                                //console.log('Redirect to: ' + redirectUrl);
                                $location.path(redirectUrl);
                            } else {
                                $location.path('/login');
                                $rootScope.error = true;
                                $rootScope.errormessage = '';
                                $scope.dataLoading = false;
                            }
                        });
                    }).error(function(data) {
                        $location.path('/login');
                        $rootScope.error = true;
                        if (data.message) {
                            $rootScope.errormessage = data.message;
                        }
                        $rootScope.authenticated = false;
                        $scope.dataLoading = false;
                    });
                }, function(data) {
                    $rootScope.error = true;
                    if (data.message) {
                        console.log("Error: " + data.message);
                        $rootScope.errormessage = data.message;
                    } else {
                        console.log("Error: " + data);
                    }
                });
            };

            angular.element(document).ready(function() {
                jQuery('#username').focus();
              });

        }]);

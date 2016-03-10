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
                        $rootScope.userid = ''+data.id;
                        $rootScope.username = data.username;
                        $rootScope.authenticated = true;
                        $rootScope.roles = [];
                        if (data.roles) {
                            for(var i in data.roles) {
                                $rootScope.roles.push(data.roles[i].name);
                            }
                        }

                        $rootScope.globals = {
                            currentUser: {
                                userid: $rootScope.userid,
                                username: $rootScope.username,
                                //credentials: $scope.credentials,
                                roles: $rootScope.roles,
                                features : [],
                                lab : null
                            }
                        };

                        $rootScope.setCurrentUserAuthorizations($rootScope.globals.currentUser);

                        _storeUserdata($rootScope.globals.currentUser);

                        if (data.authorities) {
                            for(var j in data.authorities) {
                                $rootScope.roles.push(data.authorities[j].authority);
                            }
                        }
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
                //username: 'palga@dntp.thehyve.nl',
                //password: 'palga'
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
                                    if ($rootScope.globals.currentUser.roles.indexOf('lab_user') === -1) {
                                        redirectUrl = '/';
                                    } else {
                                        redirectUrl = '/lab-requests';
                                    }
                                }
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

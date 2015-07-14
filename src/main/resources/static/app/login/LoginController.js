'use strict';

angular.module('ProcessApp.controllers')
    .controller('LoginController',['$scope', '$http', '$rootScope', '$location', '$cookieStore',
        function ($scope, $http, $rootScope, $location, $cookieStore) {



            /**
             * To authorize feature based on role
             * @param role
             */
            function setCurrentUserAuthorizations (currentUser) {

                // ========================================================================
                // TODO This might something that're organized in the backend in the future
                // ========================================================================
                var globalFeatures = {
                    HAS_MANAGE_OWN_LAB_PAGE_AUTH : 'HAS_MANAGE_OWN_LAB_PAGE_AUTH',
                    HAS_MANAGE_LAB_PAGE_AUTH : 'HAS_MANAGE_LAB_PAGE_AUTH',
                    HAS_MANAGE_USER_PAGE_AUTH : 'HAS_MANAGE_USER_PAGE_AUTH',
                    HAS_MANAGE_REQUEST_PAGE_AUTH : 'HAS_MANAGE_REQUEST_PAGE_AUTH',
                    HAS_MANAGE_LAB_REQUEST_PAGE_AUTH : 'HAS_MANAGE_LAB_REQUEST_PAGE_AUTH',
                    HAS_MANAGE_SAMPLES_PAGE_AUTH : 'HAS_MANAGE_SAMPLES_PAGE_AUTH'
                };

                if (currentUser.roles[0] === 'palga') {
                    currentUser.features.push();
                    currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_USER_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_SAMPLES_PAGE_AUTH);
                } else if (currentUser.roles[0] === 'lab_user') {
                    currentUser.features.push(globalFeatures.HAS_MANAGE_OWN_LAB_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_SAMPLES_PAGE_AUTH);
                } else if (currentUser.roles[0] === 'requester') {
                    currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                    currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                } else if (currentUser.roles[0] === 'scientific_council') {
                    currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                }
            }

            var authenticate = function(callback) {
                $http.get('user').success(function(data) {
                    // console.log('Login succes: ' + JSON.stringify(data));
                    if (data.username) {
                        $rootScope.userid = data.id;
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
                                credentials: $scope.credentials,
                                roles: $rootScope.roles,
                                features : [],
                                lab : null
                            }
                        };

                        setCurrentUserAuthorizations($rootScope.globals.currentUser);

                        $cookieStore.put('globals', $rootScope.globals);

                        if (data.authorities) {
                            for(var j in data.authorities) {
                                $rootScope.roles.push(data.authorities[j].authority);
                            }
                        }
                    } else {
                        $rootScope.authenticated = false;
                    }
                    callback && callback();
                }).error(function() {
                    $rootScope.authenticated = false;
                    callback && callback();
                });
            };

            $scope.credentials = {
                username: 'palga@dntp.thehyve.nl',
                password: 'palga'
            };

            $scope.login = function() {
                $scope.dataLoading = true;
                $http.post('login', jQuery.param($scope.credentials), {
                    headers : {
                        'content-type' : 'application/x-www-form-urlencoded'
                    }
                }).success(function(data) {
                    authenticate(function() {
                        $scope.dataLoading = false;
                        if ($rootScope.authenticated) {
                            $scope.error = false;
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
                            $scope.error = true;
                            $scope.errormessage = '';
                        }
                    });
                }).error(function(data) {
                    $location.path('/login');
                    $scope.error = true;
                    if (data.message) {
                        $scope.errormessage = data.message;
                    }
                    $rootScope.authenticated = false;
                });
            };
            
            angular.element(document).ready(function() {
                $('#username').focus();
              });

        }]);

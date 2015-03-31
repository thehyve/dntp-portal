'use strict';

angular.module('ProcessApp.controllers')
    .controller('LoginController',['$scope', '$http', '$rootScope', '$location', '$cookieStore',
        function ($scope, $http, $rootScope, $location, $cookieStore) {

            var authenticate = function(callback) {
                $http.get('user').success(function(data) {
                    if (data.name) {

                        $rootScope.userid = data.id;
                        $rootScope.username = data.name;
                        $rootScope.authenticated = true;
                        $rootScope.roles = [];
                        if (data.authorities) {
                            for(var i in data.authorities) {
                                $rootScope.roles.push(data.authorities[i].authority);
                            }
                        }
                        console.log("User '" +  data.name + "' has roles: " + JSON.stringify($rootScope.roles, null, 2));
                        
                        $rootScope.globals = {
                            currentUser: {
                                userid: $rootScope.userid,
                                username: $rootScope.username,
                                credentials: $scope.credentials,
                                roles: $rootScope.roles
                            }
                        };

                        $cookieStore.put('globals', $rootScope.globals);

                        if (data.authorities) {
                            for(var i in data.authorities) {
                                $rootScope.roles.push(data.authorities[i].authority);
                            }
                        }
                        //console.log("User '" +  data.name + "' has roles: " + JSON.stringify($rootScope.roles, null, 2));
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

            $('#username').focus();

            $scope.login = function() {
                $http.post('login', jQuery.param($scope.credentials), {
                    headers : {
                        "content-type" : "application/x-www-form-urlencoded"
                    }
                }).success(function(data) {
                    authenticate(function() {
                        if ($rootScope.authenticated) {
                            $location.path("/");
                            $scope.error = false;
                        } else {
                            $location.path("/login");
                            $scope.error = true;
                        }
                    });
                }).error(function(data) {
                    $location.path("/login");
                    $scope.error = true;
                    $rootScope.authenticated = false;
                })
            };

        }]);

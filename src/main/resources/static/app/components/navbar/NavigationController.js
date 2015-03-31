'use strict';

angular.module('ProcessApp.controllers')
    .controller('NavigationController',['$rootScope', '$scope', '$http', '$location', '$route', '$translate', '$cookieStore',
        function ($rootScope, $scope, $http, $location, $route, $translate, $cookieStore) {

            var authenticate = function(callback) {
                $http.get('user').success(function(data) {
                    if (data.name) {
    
                        $rootScope.username = data.name;
                        $rootScope.authenticated = true;
                        $rootScope.roles = [];
    
                        $rootScope.globals = {
                            currentUser: {
                                username: $rootScope.username,
                                credentials: $scope.credentials
                            }
                        };
    
                        $cookieStore.put('globals', $rootScope.globals);
    
                        if (data.authorities) {
                            for(var i in data.authorities) {
                                $rootScope.roles.push(data.authorities[i].authority);
                            }
                        }
                        console.log("User '" +  data.name + "' has roles: " + JSON.stringify($rootScope.roles, null, 2));
                    } else {
                        $rootScope.authenticated = false;
                    }
                    callback && callback();
                }).error(function() {
                    $rootScope.authenticated = false;
                    callback && callback();
                });
            };
            authenticate();

            $scope.login = function() {
                $location.path("/login");
            }
        
            $scope.logout = function() {
                $http.post('logout', {}).success(function() {
                    $location.path("/login");
                    $rootScope.authenticated = false;
                    $rootScope.globals = {};
                    $cookieStore.remove('globals');
                }).error(function(data) {
                    $rootScope.authenticated = false;
                });
            };

            $scope.currentLanguage = $translate.use();

            $scope.changeLanguage = function(langKey) {
                $translate.use(langKey);
                $scope.currentLanguage = langKey;
            };
        }]);
'use strict';

angular.module('ProcessApp.controllers')
    .controller('NavigationController',['$rootScope', '$scope', '$http', '$location', '$route', '$translate', '$cookieStore',
        function ($rootScope, $scope, $http, $location, $route, $translate, $cookieStore) {

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

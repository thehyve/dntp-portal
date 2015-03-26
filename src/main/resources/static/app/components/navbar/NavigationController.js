'use strict';

angular.module('ProcessApp.controllers')
    .controller('NavigationController',['$rootScope', '$scope', '$http', '$location', '$route', '$translate',
        function ($rootScope, $scope, $http, $location, $route, $translate) {


            $scope.logout = function() {
                $http.post('logout', {}).success(function() {
                    $rootScope.authenticated = false;
                    $location.path("/login");
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

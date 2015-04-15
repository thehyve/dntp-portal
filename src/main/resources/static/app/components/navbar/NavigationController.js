'use strict';

angular.module('ProcessApp.controllers')
    .controller('NavigationController',['$rootScope', '$scope', '$http', '$location', '$route', '$translate', '$cookieStore',
        function ($rootScope, $scope, $http, $location, $route, $translate, $cookieStore) {

            var checkRoles = function (requirements) {
                if ($rootScope.globals.hasOwnProperty('currentUser')) {
                    var userFeatures = $rootScope.globals.currentUser.features;
                    for (var j=0;j<requirements.length; j++) {
                        for (var i=0;i<userFeatures.length; i++) {
                            if (requirements[j] === userFeatures[i]) return true;
                        }
                    }
                }
                return false;
            };

            $scope.isViewLabsAllowed = function() {
                return checkRoles(['HAS_MANAGE_LAB_PAGE_AUTH']);
            };

            $scope.isViewRequestsAllowed = function() {
                return checkRoles(['HAS_MANAGE_REQUEST_PAGE_AUTH']);
            };

            $scope.isViewUsersAllowed = function() {
                return checkRoles(['HAS_MANAGE_USER_PAGE_AUTH']);
            };

            $scope.login = function() {
                $location.path("/login");
            };

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

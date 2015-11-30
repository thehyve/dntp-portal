'use strict';

angular.module('ProcessApp.controllers')
    .controller('NavigationController',['$rootScope', '$scope', '$http', '$location', '$route', '$translate', '$cookies',
        function ($rootScope, $scope, $http, $location, $route, $translate, $cookies) {

            $scope.$route = $route;
            
            var checkRoles = function (requirements) {
                if ($rootScope.globals.hasOwnProperty('currentUser')) {
                    var userFeatures = $rootScope.globals.currentUser.features;
                    for (var j=0;j<requirements.length; j++) {
                        for (var i=0;i<userFeatures.length; i++) {
                            if (requirements[j] === userFeatures[i]) {return true;}
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

            $scope.isViewLabRequestsAllowed = function() {
              return checkRoles(['HAS_MANAGE_LAB_REQUEST_PAGE_AUTH']);
            };

            $scope.isViewSamplesAllowed = function() {
                return checkRoles(['HAS_MANAGE_SAMPLES_PAGE_AUTH']);
            };

            $scope.isViewUsersAllowed = function() {
                return checkRoles(['HAS_MANAGE_USER_PAGE_AUTH']);
            };

            $scope.isViewOwnLabAllowed = function() {
                return checkRoles(['HAS_MANAGE_OWN_LAB_PAGE_AUTH']);
            };

            $scope.isViewAccessLogsAllowed = function() {
                return checkRoles(['HAS_MANAGE_ACCESS_LOG_AUTH']);
            }

            $scope.isEditAgreementFormTemplateAllowed = function() {
                return checkRoles(['HAS_MANAGE_AGREEMENT_FORM_TEMPLATE_AUTH']);
            }

            $scope.isRequestsPage = function() {
                return $route.current.templateUrl=='app/request/edit-request.html'
                    || $route.current.templateUrl=='app/request/requests.html'
                    || $route.current.templateUrl=='app/request/request.html';
            }
            
            $scope.isLabRequestsPage = function() {
                return $route.current.templateUrl=='app/lab-request/edit-lab-request.html'
                    || $route.current.templateUrl=='app/lab-request/lab-requests.html'
                    || $route.current.templateUrl=='app/lab-request/lab-request.html';
            }
            
            $scope.login = function() {
                $location.path('/login');
            };

            $scope.logout = function() {
                $http.post('logout', {}).success(function() {
                    $location.path('/login');
                    $rootScope.authenticated = false;
                    $rootScope.globals = {};
                    $cookies.remove('userid');
                    $cookies.remove('username');
                    $cookies.remove('roles');
                }).error(function(data) {
                    $rootScope.authenticated = false;
                    console.log('logout error', data);
                });
            };

            var _languages = ['nl', 'en'];
            
            $scope.changeLanguage = function(langKey) {
                if (_languages.indexOf(langKey) != -1) {
                    console.log('change language: ' + langKey);
                    $scope.currentLanguage = langKey;
                    $cookies.put('lang', $scope.currentLanguage);
                    console.log($cookies.get('lang'));
                    $translate.use(langKey);
                    bootbox.setDefaults({locale: langKey});
                }
            };

            $scope.currentLanguage = $cookies.get('lang');
            if ($scope.currentLanguage) {
                $translate.use($scope.currentLanguage);
                bootbox.setDefaults({locale: $scope.currentLanguage});
            } else {
                $scope.changeLanguage($translate.use());
            }

        }]);

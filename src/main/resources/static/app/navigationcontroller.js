(function(angular) {

    var NavigationController = function($rootScope, $scope, $http, $location, $route, $translate) {

        $scope.$route = $route;
        
        var authenticate = function(callback) {
            $http.get('user').success(function(data) {
                if (data.name) {
                    $rootScope.username = data.name;
                    $rootScope.authenticated = true;
                } else {
                    $rootScope.authenticated = false;
                }
                callback && callback();
            }).error(function() {
                $rootScope.authenticated = false;
                callback && callback();
            });

        }

        authenticate();

        $scope.credentials = {};

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

        $scope.logout = function() {
            $http.post('logout', {}).success(function() {
                $rootScope.authenticated = false;
                $location.path("/");
            }).error(function(data) {
                $rootScope.authenticated = false;
            });
        }
        
        $scope.currentLanguage = $translate.use();
        
        $scope.changeLanguage = function(langKey) {
            $translate.use(langKey);
            $scope.currentLanguage = langKey;
        };
    };

    angular.module("ProcessApp.controllers").controller("NavigationController",
            NavigationController);
}(angular));
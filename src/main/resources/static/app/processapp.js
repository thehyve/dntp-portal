(function(angular) {
    angular.module("ProcessApp.services", []);
    angular.module("ProcessApp.directives", []);
    angular.module("ProcessApp.controllers", ["restangular"])
        .config(function(RestangularProvider) {
            RestangularProvider.setBaseUrl('/');
        });
    angular.module('ProcessApp', [ "flow",
                                   "mgcrea.ngStrap", 
                                   "ngResource", "ngRoute", "ngCookies",
                                   "pascalprecht.translate",
                                   "smart-table",
                                   "ProcessApp.services", "ProcessApp.controllers",
                                   "ProcessApp.directives"])
        .config(function($routeProvider, $translateProvider) {

            $routeProvider.when('/', {
                templateUrl : 'workflow.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'app/components/login/login.html',
                controller : 'NavigationController'
            }).when('/login/forgot-password', {
                templateUrl : 'app/components/forgot-password/forgot-password.html',
                controller : 'ForgotPasswordController'
            }).when('/login/reset-password/:token', {
                templateUrl : 'app/components/forgot-password/reset-password.html',
                controller : 'ResetPasswordController'
            }).when('/register', {
                templateUrl : 'app/components/registration/registration.html',
                controller : 'RegistrationController'
            }).when('/users', {
                templateUrl : 'app/components/admin/users.html'
            }).when('/labs', {
                templateUrl : 'app/components/admin/labs.html'
            }).when('/profile/password', {
                templateUrl : 'app/components/profile/password.html',
                controller : 'PasswordController'
            }).when('/profile/', {
                templateUrl : 'app/components/profile/profile.html',
                controller : 'ProfileController'
            }).otherwise('/');
            
            $translateProvider.translations('en', messages_en)
                              .translations('nl', messages_nl);
            $translateProvider.preferredLanguage('en');
        })

        .run(['$rootScope', '$location', '$cookieStore', '$http',
            function ($rootScope, $location, $cookieStore, $http) {

                // keep user logged in after page refresh
                $rootScope.globals = $cookieStore.get('globals') || {};

                if ($rootScope.globals.currentUser) {
                    $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.credentials;
                }

                $rootScope.$on('$locationChangeStart', function (event, next, current) {
                    // redirect to login page if not logged in
                    if (($location.path() !== '/login'
                        && $location.path() !== '/register'
                        && $location.path() !== '/login/forgot-password'
                        && $location.path() !== '/login/reset-password'
                        ) && !$rootScope.globals.currentUser) {
                        $location.path('/login');
                    }
                });
            }]);

}(angular));

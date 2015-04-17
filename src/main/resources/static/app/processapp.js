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
                templateUrl : 'app/components/dashboard/dashboard.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'app/components/login/login.html',
                controller : 'LoginController'
            }).when('/login/forgot-password', {
                templateUrl : 'app/components/forgot-password/forgot-password.html',
                controller : 'ForgotPasswordController'
            }).when('/login/reset-password/:token', {
                templateUrl : 'app/components/forgot-password/reset-password.html',
                controller : 'ResetPasswordController'
            }).when('/register', {
                templateUrl : 'app/components/registration/registration.html',
                controller : 'RegistrationController'
            }).when('/register/success', {
                templateUrl : 'app/components/registration/thank-you.html',
                controller : 'RegistrationController'
            }).when('/users', {
                templateUrl : 'app/components/admin/users.html'
            }).when('/labs', {
                templateUrl : 'app/components/admin/labs.html'
            }).when('/my-lab', {
                templateUrl : 'app/components/lab/my-lab.html',
                controller : 'MyLabController'
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
                    $rootScope.authenticated = true;
                    $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.credentials;
                }

                $rootScope.$on('$locationChangeStart', function (event, next, current) {
                    // redirect to login page if not logged in
                    if (($location.path() !== '/login'
                        && $location.path() !== '/register'
                        && $location.path() !== '/register/success'
                        && $location.path() !== '/login/forgot-password'
                        && !startsWith($location.path(), '/login/reset-password')
                        ) && !$rootScope.globals.currentUser) {
                        $location.path('/login');
                    }
                });
            }]);


    // Checks if `string` starts with `start`
    function startsWith(string, start) {
        console.log('startsWith: ' + string + ', ' + start);
        if (typeof(string) !== 'string')
            return false;

        if (string.length < start.length)
            return false;

        for (var i = 0; i < string.length && i < start.length; i++)
            if (string[i] !== start[i])
                return false;

        return true;
    }
}(angular));

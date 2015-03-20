(function(angular) {
    angular.module("ProcessApp.services", []);
    angular.module("ProcessApp.controllers", []);
    angular.module('ProcessApp', [ "flow", 
                                   "ngResource", "ngRoute", "ngCookies",
                                   "pascalprecht.translate",
                                   "ProcessApp.services", "ProcessApp.controllers" ])
        .config(['$routeProvider', '$translateProvider', function($routeProvider, $translateProvider) {
            $routeProvider.when('/', {
                templateUrl : 'workflow.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'app/components/login/login.html',
                controller : 'NavigationController'
            }).when('/register', {
                templateUrl : 'app/components/registration/registration.html',
                controller : 'RegistrationController'
            }).when('/users', {
                templateUrl : 'app/components/admin/users.html'
            }).when('/labs', {
                templateUrl : 'app/components/admin/labs.html'
            }).when('/institutions', {
                templateUrl : 'app/components/admin/institutions.html'
            }).otherwise('/');
            
            $translateProvider.translations('en', messages_en)
                              .translations('nl', messages_nl);
            $translateProvider.preferredLanguage('en');

        }])

        .run(['$rootScope', '$location', '$cookieStore', '$http',
            function ($rootScope, $location, $cookieStore, $http) {

                // keep user logged in after page refresh
                $rootScope.globals = $cookieStore.get('globals') || {};

                if ($rootScope.globals.currentUser) {
                    $http.defaults.headers.common['Authorization'] = 'Basic ' + $rootScope.globals.currentUser.credentials;
                }

                $rootScope.$on('$locationChangeStart', function (event, next, current) {
                    // redirect to login page if not logged in
                    if (($location.path() !== '/login' && $location.path() !== '/register') && !$rootScope.globals.currentUser) {
                        $location.path('/login');
                    }
                });
            }]);

}(angular));

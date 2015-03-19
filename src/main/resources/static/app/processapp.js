(function(angular) {
    angular.module("ProcessApp.services", []);
    angular.module("ProcessApp.directives", []);
    angular.module("ProcessApp.controllers", ["restangular"]);
    angular.module('ProcessApp', [ "flow",
                                   "ngResource", "ngRoute", "ngCookies",
                                   "pascalprecht.translate",
                                   "ProcessApp.services", "ProcessApp.controllers",
                                   "ProcessApp.directives"])
        .config(function($routeProvider, $translateProvider) {
            $routeProvider.when('/', {
                templateUrl : 'workflow.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'login.html',
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
            }).when('/institutions', {
                templateUrl : 'app/components/admin/institutions.html'
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
      });

}(angular));
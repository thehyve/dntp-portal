(function(angular) {
    angular.module("ProcessApp.services", []);
    angular.module("ProcessApp.controllers", ["restangular"]);
    angular.module('ProcessApp', [ "flow",
                                   "ngResource", "ngRoute", "ngCookies",
                                   "pascalprecht.translate",
                                   "ProcessApp.services", "ProcessApp.controllers" ])
        .config(function($routeProvider, $translateProvider) {
            $routeProvider.when('/', {
                templateUrl : 'workflow.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'login.html',
                controller : 'NavigationController'
            }).when('/register', {
                templateUrl : 'app/components/registration/registration.html',
                controller : 'RegistrationController'
            }).when('/admin', {
                templateUrl : 'admin.html'
            }).when('/profile', {
                templateUrl : 'app/components/profile/profile.html',
                controller : 'ProfileController'
            }).when('/profile/password', {
                templateUrl : 'app/components/profile/password.html',
                controller : 'PasswordController'
            }).otherwise('/');
            
            $translateProvider.translations('en', messages_en)
                              .translations('nl', messages_nl);
            $translateProvider.preferredLanguage('en');
      });

}(angular));
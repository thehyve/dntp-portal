'use strict';

(function(angular) {

    angular.module('ProcessApp.services', ['restangular'])
      .config(function(RestangularProvider) {
        RestangularProvider.setBaseUrl('/');
      });
    angular.module('ProcessApp.directives', []);
    angular.module('ProcessApp.controllers', ['restangular'])
      .config(function(RestangularProvider) {
        RestangularProvider.setBaseUrl('/');
      });
    angular.module('ProcessApp', [ 'flow',
                                   'mgcrea.ngStrap',
                                   'ngResource', 'ngRoute', 'ngCookies',
                                   'pascalprecht.translate',
                                   'smart-table', 'ngSanitize',
                                   'ProcessApp.services',
                                   'ProcessApp.controllers',
                                   'ProcessApp.directives'])
        .config(function($routeProvider, $translateProvider, $popoverProvider) {

            $routeProvider.when('/', {
                templateUrl : 'app/request/requests.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'app/login/login.html',
                controller : 'LoginController'
            }).when('/login/forgot-password', {
                templateUrl : 'app/forgot-password/forgot-password.html',
                controller : 'ForgotPasswordController'
            }).when('/login/reset-password/:token', {
                templateUrl : 'app/forgot-password/reset-password.html',
                controller : 'ResetPasswordController'
            }).when('/register', {
                templateUrl : 'app/registration/registration.html',
                controller : 'RegistrationController'
            }).when('/register/success', {
                templateUrl : 'app/registration/thank-you.html',
                controller : 'RegistrationController'
            }).when('/activate/:token', {
                templateUrl : 'app/activation/activate.html',
                controller : 'ActivationController'
            }).when('/users', {
                templateUrl : 'app/admin/user/users.html'
            }).when('/labs', {
                templateUrl : 'app/admin/lab/labs.html'
            }).when('/lab-requests', {
                templateUrl : 'app/lab-request/lab-requests.html',
                controller : ''
            }).when('/lab-request/view/:labRequestId', {
                templateUrl : 'app/lab-request/lab-request.html',
                controller : ''
            }).when('/request/view/:requestId', {
                templateUrl : 'app/request/request.html',
                controller : 'RequestController'
            }).when('/request/view/:requestId', {
                templateUrl : 'app/request/request.html',
                controller : 'RequestController'
            }).when('/request/edit/:requestId', {
                templateUrl : 'app/request/edit-request.html',
                controller : 'RequestController'
            }).when('/request/:requestId/selection', {
                templateUrl : 'app/request/selection.html',
                controller : 'RequestController'
            }).when('/my-lab', {
                templateUrl : 'app/lab/my-lab.html',
                controller : 'MyLabController'
            }).when('/profile/password', {
                templateUrl : 'app/profile/password.html',
                controller : 'PasswordController'
            }).when('/profile/', {
                templateUrl : 'app/profile/profile.html',
                controller : 'ProfileController'
            }).otherwise('/');

            // default lang settings for the localization
            $translateProvider.translations('en', messagesEN)
                              .translations('nl', messagesNL);
            $translateProvider.preferredLanguage('en');
            $translateProvider.useSanitizeValueStrategy('escaped');

            // default popover setting to have html friendly content
            angular.extend($popoverProvider.defaults, {
                html: true,
                trigger: 'hover'
            });

        })

        .run(['$rootScope', '$location', '$cookieStore', '$http',
            function ($rootScope, $location, $cookieStore, $http) {

                // keep user logged in after page refresh
                $rootScope.globals = $cookieStore.get('globals') || {};

                if ($rootScope.globals.currentUser) {
                    $rootScope.authenticated = true;
                    $http.defaults.headers.common.Authorization = 'Basic ' + $rootScope.globals.currentUser.credentials;
                }

                $rootScope.$on('$locationChangeStart', function () {
                    // redirect to login page if not logged in
                  if (($location.path() !== '/login' &&
                    $location.path() !== '/register' &&
                    $location.path() !== '/register/success' &&
                    $location.path() !== '/login/forgot-password' &&
                    !startsWith($location.path(), '/login/reset-password') &&
                    !startsWith($location.path(), '/activate/')
                    ) &&
                    !$rootScope.globals.currentUser) {
                    $location.path('/login');
                  }
                });
            }]);


    // Checks if `string` starts with `start`
    function startsWith(string, start) {
        if (typeof(string) !== 'string') {
          return false;
        }
        if (string.length < start.length) {
          return false;
        }
        for (var i = 0; i < string.length && i < start.length; i++) {
          if (string[i] !== start[i]) {
            return false;
          }
        }
        return true;
    }
}(angular));

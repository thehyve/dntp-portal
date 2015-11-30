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
    angular.module('ProcessApp', ['flow',
        'mgcrea.ngStrap',
        'ngResource', 'ngRoute', 'ngCookies',
        'pascalprecht.translate', 'ngTagsInput',
        'smart-table', 'ngSanitize',
        'angular-loading-bar',
        'ProcessApp.services',
        'ProcessApp.controllers',
        'ProcessApp.directives',
        'ng-showdown'])
        .config(function(
                $routeProvider, 
                $translateProvider, 
                $popoverProvider,
                cfpLoadingBarProvider,
                $showdownProvider) {

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
            }).when('/accesslogs', {
                templateUrl : 'app/admin/accesslogs/accesslogs.html'
            }).when('/accesslogs/:filename', {
                templateUrl : 'app/admin/accesslogs/accesslogs.html'
            }).when('/agreementformtemplate', {
                templateUrl : 'app/admin/agreementformtemplate/edit.html'
            }).when('/agreementformtemplate/:action', {
                templateUrl : 'app/admin/agreementformtemplate/help.html'
            }).when('/lab-requests', {
                templateUrl : 'app/lab-request/lab-requests.html',
                controller : ''
            }).when('/lab-request/view/:labRequestId', {
                templateUrl : 'app/lab-request/lab-request.html',
                controller : ''
            }).when('/samples', {
                templateUrl : 'app/lab-request/samples.html',
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
            }).when('/request/:requestId/agreementform', {
                templateUrl : 'app/request/agreementform.html'
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

            // Try to fetch the preferred language from the browser.
            var language = window.navigator.userLanguage || window.navigator.language;
            var preferredLanguage = 'nl';
            if (language != null && language.length >= 2) {
                language = language.substring(0, 2);
                var supported_languages = ['nl', 'en'];
                if (supported_languages.indexOf(language) != -1)
                {
                    preferredLanguage = language;
                }
            }

            // default lang settings for the localization
            $translateProvider.translations('en', messagesEN)
                              .translations('nl', messagesNL);
            $translateProvider.preferredLanguage(preferredLanguage);
            $translateProvider.useSanitizeValueStrategy('escaped');
            cfpLoadingBarProvider.includeSpinner = false;

            // default popover setting to have html friendly content
            angular.extend($popoverProvider.defaults, {
                html: true,
                trigger: 'hover'
            });

            // set showdown  provider
            $showdownProvider.setOption('tables', true)
        })

        .run(['$rootScope', '$location', '$cookies', '$http',
            function ($rootScope, $location, $cookies, $http) {

                /**
                 * To authorize feature based on role
                 * @param role
                 */
                $rootScope.setCurrentUserAuthorizations = function(currentUser) {
    
                    // ========================================================================
                    // TODO This might something that're organized in the backend in the future
                    // ========================================================================
                    var globalFeatures = {
                        HAS_MANAGE_OWN_LAB_PAGE_AUTH : 'HAS_MANAGE_OWN_LAB_PAGE_AUTH',
                        HAS_MANAGE_LAB_PAGE_AUTH : 'HAS_MANAGE_LAB_PAGE_AUTH',
                        HAS_MANAGE_USER_PAGE_AUTH : 'HAS_MANAGE_USER_PAGE_AUTH',
                        HAS_MANAGE_REQUEST_PAGE_AUTH : 'HAS_MANAGE_REQUEST_PAGE_AUTH',
                        HAS_MANAGE_LAB_REQUEST_PAGE_AUTH : 'HAS_MANAGE_LAB_REQUEST_PAGE_AUTH',
                        HAS_MANAGE_SAMPLES_PAGE_AUTH : 'HAS_MANAGE_SAMPLES_PAGE_AUTH',
                        HAS_MANAGE_ACCESS_LOG_AUTH : 'HAS_MANAGE_ACCESS_LOG_AUTH',
                        HAS_MANAGE_AGREEMENT_FORM_TEMPLATE_AUTH: 'HAS_MANAGE_AGREEMENT_FORM_TEMPLATE_AUTH'
                    };
    
                    if (currentUser.roles[0] === 'palga') {
                        currentUser.features.push();
                        currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_USER_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_SAMPLES_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_ACCESS_LOG_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_AGREEMENT_FORM_TEMPLATE_AUTH);
                    } else if (currentUser.roles[0] === 'lab_user') {
                        currentUser.features.push(globalFeatures.HAS_MANAGE_OWN_LAB_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_SAMPLES_PAGE_AUTH);
                    } else if (currentUser.roles[0] === 'requester') {
                        currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                    } else if (currentUser.roles[0] === 'scientific_council') {
                        currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                    }
                };
            
                var _deserialiseRoles = function(text) {
                    var result = text.split(',');
                    return result;
                };
                
                var _fetchUserdata = function() {
                    var userid = $cookies.get('userid');
                    if (!userid) { return null; }
                    var userdata = {
                        userid: userid,
                        username: $cookies.get('username'),
                        roles: _deserialiseRoles($cookies.get('roles')),
                        features : [],
                        lab : null
                    };
                    return userdata;
                };
            
                // keep user logged in after page refresh
                var userdata = _fetchUserdata();
                $rootScope.globals = userdata ? { currentUser: userdata } : {};

                if ($rootScope.globals.currentUser) {
                    $rootScope.authenticated = true;
                    $rootScope.setCurrentUserAuthorizations($rootScope.globals.currentUser);
                    //$http.defaults.headers.common.Authorization = 'Basic ' + $rootScope.globals.currentUser.credentials;
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
                      $rootScope.redirectUrl = $location.path();
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

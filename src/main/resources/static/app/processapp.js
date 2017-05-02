/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, window, jQuery, _, angular, messages) {
    'use strict';

    angular.module('ProcessApp.services', ['mgcrea.ngStrap.alert','ngCookies', 'restangular'])
        .config(function ( $alertProvider, $cookiesProvider, RestangularProvider ) {
            RestangularProvider.setBaseUrl('/');
        });
    angular.module('ProcessApp.interceptors', []);
    angular.module('ProcessApp.directives', ['pascalprecht.translate']);
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
        'ProcessApp.interceptors',
        'ProcessApp.directives',
        'ProcessApp.controllers',
        'textAngular','isteven-multi-select'])
        .config(function(
                $routeProvider,
                $httpProvider,
                $translateProvider, 
                $popoverProvider,
                cfpLoadingBarProvider) {

            $routeProvider.when('/', {
                templateUrl : 'app/request/requests.html',
                controller : ''
            }).when('/requests/:selection', {
                templateUrl : 'app/request/requests.html',
                controller : ''
            }).when('/login', {
                templateUrl : 'app/login/login.html',
                controller : 'LoginController'
            }).when('/login/forgot-password', {
                templateUrl : 'app/forgot-password/forgot-password.html',
                controller : ''
            }).when('/login/reset-password/:token', {
                templateUrl : 'app/forgot-password/reset-password.html',
                controller : ''
            }).when('/register', {
                templateUrl : 'app/registration/registration.html',
                controller : ''
            }).when('/register/success', {
                templateUrl : 'app/registration/thank-you.html',
                controller : ''
            }).when('/activate/:token', {
                templateUrl : 'app/activation/activate.html',
                controller : ''
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
            }).when('/lab-requests', {
                templateUrl : 'app/lab-request/lab-requests.html',
                controller : ''
            }).when('/lab-requests/:selection', {
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
                templateUrl : 'app/lab/my-lab.html'
            }).when('/hub-labs', {
                templateUrl : 'app/lab/hub-labs.html',
                controller : 'HubLabsController'
            }).when('/profile/password', {
                templateUrl : 'app/profile/password.html',
                controller : 'PasswordController'
            }).when('/profile/', {
                templateUrl : 'app/profile/profile.html',
                controller : ''
            }).otherwise('/');

            // Try to fetch the preferred language from the browser.
            var language = window.navigator.userLanguage || window.navigator.language;
            var preferredLanguage = 'nl';
            if (language !== null && language.length >= 2) {
                language = language.substring(0, 2);
                var supported_languages = ['nl', 'en'];
                if (supported_languages.indexOf(language) != -1)
                {
                    preferredLanguage = language;
                }
            }

            // default lang settings for the localization
            $translateProvider.translations('en', messages.en)
                              .translations('nl', messages.nl);
            $translateProvider.preferredLanguage(preferredLanguage);
            $translateProvider.useSanitizeValueStrategy('escaped');
            cfpLoadingBarProvider.includeSpinner = false;

            // Inject response interceptor for error handling
            $httpProvider.interceptors.push('responseObserver');

            // default popover setting to have html friendly content
            angular.extend($popoverProvider.defaults, {
                html: true,
                trigger: 'hover'
            });

        })

        .run(['$rootScope', '$location', '$cookies', '$http', '$alert', '$translate', 'LoginService',
            function ($rootScope, $location, $cookies, $http, $alert, $translate, LoginService) {

                /**
                 * To authorize feature based on role
                 * @param role
                 */
                $rootScope.setCurrentUserAuthorizations = function(currentUser) {

                    // ========================================================================
                    // TODO This might something that're organized in the backend in the future
                    // ========================================================================
                    var globalFeatures = {
                        HAS_MANAGE_HUB_LABS_PAGE_AUTH : 'HAS_MANAGE_HUB_LABS_PAGE_AUTH',
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
                    } else if (currentUser.roles[0] === 'hub_user') {
                        currentUser.features.push(globalFeatures.HAS_MANAGE_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_LAB_REQUEST_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_SAMPLES_PAGE_AUTH);
                        currentUser.features.push(globalFeatures.HAS_MANAGE_HUB_LABS_PAGE_AUTH);
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

                $rootScope.updateUserData = function(userdata) {
                    $rootScope.globals = userdata ? { currentUser: userdata } : {};

                    if ($rootScope.globals.currentUser) {
                        $rootScope.authenticated = true;
                        $rootScope.setCurrentUserAuthorizations($rootScope.globals.currentUser);
                        //$http.defaults.headers.common.Authorization = 'Basic ' + $rootScope.globals.currentUser.credentials;
                    }

                    $rootScope.currentUserId = _.get($rootScope, 'globals.currentUser.userid');
                    $rootScope.currentUsername = _.get($rootScope, 'globals.currentUser.username', '');
                    $rootScope.currentRole = _.find(_.get($rootScope, 'globals.currentUser.roles', []));
                };

                // keep user logged in after page refresh
                var userdata = _fetchUserdata();
                $rootScope.updateUserData(userdata);

                var _getName = function(user) {
                    if (user === null) {
                        return '';
                    }
                    return _.compact([user.firstName, user.lastName]).join(' ');
                };
                $rootScope.getName = _getName;

                $rootScope.focus = function(el) {
                    jQuery(el).focus();
                };

                $rootScope.translate = function(key, params) {
                    return $translate.instant(key, params);
                };

                $rootScope.heartbeat = function() {
                    return $http.get('user');
                };

                $rootScope.isCurrentUser = function(userid) {
                    return ($rootScope.currentUserId === userid);
                };

                $rootScope.hasRole = function(role) {
                    return _.includes(_.get($rootScope, 'globals.currentUser.roles', []), role);
                };

                $rootScope.isPalga = function() {
                    return $rootScope.hasRole('palga');
                };

                $rootScope.isRequester = function() {
                    return $rootScope.hasRole('requester');
                };

                $rootScope.isMyRequest = function (req) {
                    var isMyReq = false;
                    if (req && req.hasOwnProperty('requesterId')) {
                        isMyReq = req.requesterId.toString() === $rootScope.currentUserId;
                    }
                    return isMyReq;
                };

                $rootScope.isLabOrHubUser = function() {
                    return $rootScope.isLabUser() || $rootScope.isHubUser();
                };

                $rootScope.isLabUser = function() {
                    return $rootScope.hasRole('lab_user');
                };

                $rootScope.isHubUser = function() {
                    return $rootScope.hasRole('hub_user');
                };

                $rootScope.isScientificCouncil = function() {
                    return $rootScope.hasRole('scientific_council');
                };

                $rootScope.showLoginModal = function(reloadPageAfterLogin) {
                    LoginService.showLogin(reloadPageAfterLogin);
                };

                $rootScope.logErrorResponse = function(response) {
                    var message = _.get(response, 'data.message', '<empty>');
                    console.log('Status ' + response.status + ': ' + _.get(response, 'data.error', 'Error') + '\n' + message);
                };

                $rootScope.alert = function(options) {
                    return $alert(options);
                };

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

                $rootScope.timeCache = 0;

                $rootScope.getDatetime = function() {
                    var now = Date.now();
                    var diff = Math.abs(now - $rootScope.timeCache);
                    if (diff > 100){
                        $rootScope.timeCache = now;
                        return now;
                    } else {
                        return $rootScope.timeCache;
                    }
                };

                $rootScope.getDatetimeOneHalfYear = function() {
                    var now = Date.now();
                    var diff = Math.abs(now - $rootScope.timeCache);
                    if (diff > 100){
                        $rootScope.timeCache = now;
                    } else {
                        now = $rootScope.timeCache;
                    }
                    return now + (1.5 * 365 * 24 * 60 * 60 * 1000)
                };
            }])
        .filter("statusTextFilter", function ($filter) {
            return function(input, predicate){
                // Strict search only for the statusText column, because values can be substrings of other values causing them both to show up.
                if (predicate.hasOwnProperty('statusText')){
                    // First filter for items with the relevant statusText, then apply other search criteria. 
                    var status_filtered = $filter('filter')(input, {'statusText': predicate['statusText']}, true);
                    delete predicate['statusText'];
                    return $filter('filter')(status_filtered, predicate, false);
                } else {
                    return $filter('filter')(input, predicate, false);
                }
            }
        });

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
})(console, window, jQuery, _, angular, window.messages);

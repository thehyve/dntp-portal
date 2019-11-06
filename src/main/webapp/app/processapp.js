/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

require('bootstrap/dist/css/bootstrap.css');
require('font-awesome/css/font-awesome.css');
require('@mdi/font/css/materialdesignicons.css');

require('ng-tags-input/build/ng-tags-input.css');
require('ng-tags-input/build/ng-tags-input.bootstrap.css');
require('angular-loading-bar/build/loading-bar.css');
require('github-markdown-css/github-markdown.css');
require('textangular/dist/textAngular.css');
require('isteven-angular-multiselect/isteven-multi-select.css');

require('../css/index.less');
require('../css/index.css');
require('../css/progressBar.css');

import 'core-js/stable';
require('bootstrap/dist/js/bootstrap');
import angular from 'angular';
require('@flowjs/ng-flow/dist/ng-flow');
require('angular-strap/dist/angular-strap');
require('angular-strap/dist/angular-strap.tpl');
import ngResource from 'angular-resource';
import ngRoute from 'angular-route';
import ngCookies from 'angular-cookies';
import restangular from 'restangular';
import translate from 'angular-translate';
require('ng-tags-input');
import smartTable from 'angular-smart-table';
import ngSanitize from 'angular-sanitize';
import ngLoadingBar from 'angular-loading-bar';
require('rangy/lib/rangy-core');
require('rangy/lib/rangy-selectionsaverestore');
// Workaround because textangular has not been updated to be compatible with newer versions of Angular
if (!angular.lowercase) {
    angular.lowercase = str => angular.isString(str) ? str.toLowerCase() : str;
}
require('textangular/dist/textAngular-sanitize');
//require('textangular/dist/textAngularSetup');
require('textangular/dist/textAngular.min');
require('isteven-angular-multiselect/isteven-multi-select.js');

import en from '../messages/messages_en';
import nl from '../messages/messages_nl';

angular.module('ProcessApp.services', ['mgcrea.ngStrap.alert', ngCookies, restangular])
    .config(['$alertProvider', '$cookiesProvider', 'RestangularProvider', function ($alertProvider, $cookiesProvider, RestangularProvider) {
        RestangularProvider.setBaseUrl('/');
    }]);
require('./services');
angular.module('ProcessApp.interceptors', []);
require('./interceptors');
angular.module('ProcessApp.directives', [translate]);
angular.module('ProcessApp.controllers', [restangular])
  .config(['RestangularProvider', function(RestangularProvider) {
    RestangularProvider.setBaseUrl('/');
  }]);

require('./activation');
require('./admin');
const components = require('./components');
require('./forgot-password');
require('./lab');
require('./lab-request');
const login = require('./login');
require('./profile');
require('./registration');
require('./request');

angular.module('ProcessApp', ['flow',
    'mgcrea.ngStrap', 'mgcrea.ngStrap.popover',
    ngResource, ngRoute, ngCookies,
    translate, 'ngTagsInput',
    smartTable, ngSanitize,
    ngLoadingBar,
    'ProcessApp.services',
    'ProcessApp.interceptors',
    'ProcessApp.directives',
    'ProcessApp.controllers',
    'textAngular', 'isteven-multi-select'])
    .config(['$routeProvider',
        '$httpProvider',
        '$locationProvider',
        '$translateProvider',
        '$popoverProvider',
        'cfpLoadingBarProvider', function(
            $routeProvider,
            $httpProvider,
            $locationProvider,
            $translateProvider,
            $popoverProvider,
            cfpLoadingBarProvider) {

        $locationProvider.hashPrefix('');

        $routeProvider.when('/', {
            template : require('./request/requests.html'),
            controller : ''
        }).when('/requests/:selection', {
            template : require('./request/requests.html'),
            controller : ''
        }).when('/login', {
            template : require('./login/login.html'),
            controller : 'LoginController'
        }).when('/login/forgot-password', {
            template : require('./forgot-password/forgot-password.html'),
            controller : ''
        }).when('/login/reset-password/:token', {
            template : require('./forgot-password/reset-password.html'),
            controller : ''
        }).when('/register', {
            template : require('./registration/registration.html'),
            controller : ''
        }).when('/register/success', {
            template : require('./registration/thank-you.html'),
            controller : ''
        }).when('/activate/:token', {
            template : require('./activation/activate.html'),
            controller : ''
        }).when('/users', {
            template : require('./admin/user/users.html')
        }).when('/labs', {
            template : require('./admin/lab/labs.html')
        }).when('/accesslogs', {
            template : require('./admin/accesslogs/accesslogs.html')
        }).when('/accesslogs/:filename', {
            template : require('./admin/accesslogs/accesslogs.html')
        }).when('/agreementformtemplate', {
            template : require('./admin/agreementformtemplate/edit.html')
        }).when('/lab-requests', {
            template : require('./lab-request/lab-requests.html'),
            controller : ''
        }).when('/lab-requests/:selection', {
            template : require('./lab-request/lab-requests.html'),
            controller : ''
        }).when('/lab-request/view/:labRequestId', {
            template : require('./lab-request/lab-request.html'),
            controller : ''
        }).when('/samples', {
            template : require('./lab-request/samples.html'),
            controller : ''
        }).when('/request/view/:requestId', {
            template : require('./request/request.html'),
            controller : 'RequestController'
        }).when('/request/view/:requestId', {
            template : require('./request/request.html'),
            controller : 'RequestController'
        }).when('/request/edit/:requestId', {
            template : require('./request/edit-request.html'),
            controller : 'RequestController'
        }).when('/request/:requestId/selection', {
            template : require('./request/selection.html'),
            controller : 'RequestController'
        }).when('/request/:requestId/agreementform', {
            template : require('./request/agreementform.html')
        }).when('/my-lab', {
            template : require('./lab/my-lab.html')
        }).when('/hub-labs', {
            template : require('./lab/hub-labs.html'),
            controller : 'HubLabsController'
        }).when('/profile/password', {
            template : require('./profile/password.html'),
            controller : 'PasswordController'
        }).when('/profile/', {
            template : require('./profile/profile.html'),
            controller : ''
        }).otherwise('/');

        // Try to fetch the preferred language from the browser.
        let language = window.navigator.userLanguage || window.navigator.language;
        let preferredLanguage = 'nl';
        if (language !== null && language.length >= 2) {
            language = language.substring(0, 2);
            var supported_languages = ['nl', 'en'];
            if (supported_languages.indexOf(language) !== -1)
            {
                preferredLanguage = language;
            }
        }

        // default lang settings for the localization
        $translateProvider.translations('en', en)
                          .translations('nl', nl);
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

    }])

    .run(['$rootScope', '$location', '$cookies', '$http', '$alert', '$translate', 'LoginService', '$templateCache',
        function ($rootScope, $location, $cookies, $http, $alert, $translate, LoginService, $templateCache) {

            for (let template in components.templates) {
                $templateCache.put(template, components.templates[template]);
            }
            for (let template in login.templates) {
                $templateCache.put(template, login.templates[template]);
            }

            /**
             * To authorize feature based on role
             * @param currentUser
             */
            $rootScope.setCurrentUserAuthorizations = function(currentUser) {

                // ========================================================================
                // TODO This might something that're organized in the backend in the future
                // ========================================================================
                const globalFeatures = {
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

            const _deserialiseRoles = function(text) {
                return text.split(',');
            };

            const _fetchUserdata = function() {
                const userid = $cookies.get('userid');
                if (!userid) { return null; }
                return {
                    userid: userid,
                    username: $cookies.get('username'),
                    roles: _deserialiseRoles($cookies.get('roles')),
                    features : [],
                    lab : null
                };
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
            const userdata = _fetchUserdata();
            $rootScope.updateUserData(userdata);

            const _getName = function(user) {
                if (user === null) {
                    return '';
                }
                return _.compact([user.firstName, user.lastName]).join(' ');
            };
            $rootScope.getName = _getName;

            $rootScope.focus = function(el) {
                jQuery(el).focus();
            };

            $rootScope.isPublicPage = function() {
                return _.includes([
                        '/login',
                        '/register',
                        '/register/success',
                        '/login/forgot-password'
                    ], $location.path()) ||
                    startsWith($location.path(), '/login/reset-password') ||
                    startsWith($location.path(), '/activate/');
            };

            $rootScope.showNavbar = function() {
                return !$rootScope.isPublicPage();
            };

            $rootScope.showLoginHeader = function() {
                return $rootScope.isPublicPage();
            };

            $rootScope.translate = function(key, params) {
                return $translate.instant(key, params);
            };

            $rootScope.heartbeat = function() {
                return $http.get('/api/user');
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
                let isMyReq = false;
                if (req && Object.prototype.hasOwnProperty.call(req,'requesterId')) {
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
                if (!$rootScope.isPublicPage() && !$rootScope.globals.currentUser) {
                    $rootScope.redirectUrl = $location.path();
                    $location.path('/login');
                }
            });

            $rootScope.timeCache = 0;

            $rootScope.getDatetime = function() {
                const now = Date.now();
                const diff = Math.abs(now - $rootScope.timeCache);
                if (diff > 100){
                    $rootScope.timeCache = now;
                    return now;
                } else {
                    return $rootScope.timeCache;
                }
            };
        }])
    .filter("statusTextFilter", function ($filter) {
        return function(input, predicate){
            // Strict search only for the statusText column, because values can be substrings of other values causing them both to show up.
            if (Object.prototype.hasOwnProperty.call(predicate,'statusText')){
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
    for (let i = 0; i < string.length && i < start.length; i++) {
      if (string[i] !== start[i]) {
        return false;
      }
    }
    return true;
}

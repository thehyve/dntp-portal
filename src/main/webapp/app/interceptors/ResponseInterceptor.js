/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.interceptors')
    .factory('responseObserver', ['$rootScope', '$cookies', '$location', '$q',
                                  function ($rootScope, $cookies, $location, $q) {
        var _logout = function() {
            $rootScope.authenticated = false;
            $rootScope.globals = {};
            $cookies.remove('userid');
            //$cookies.remove('username');
            $cookies.remove('roles');
            $rootScope.error = false;
            $rootScope.errormessage = '';
        };

        return {
            'responseError': function(response) {
                var method = _.get(response, 'config.method', '');
                var url = _.get(response, 'config.url', '').trim();

                switch(response.status) {
                case 302:
                case 403:
                    if (url === 'user') {
                        //console.log('Error fetching user data.');
                        $rootScope.logErrorResponse(response);
                        return $q.reject(response);
                    }
                    return $rootScope.heartbeat().then(function() {
                        // User is logged in, but denied access; redirect to start page.
                        //console.log('User is logged in, but denied access; redirect to start page.');
                        $rootScope.alert({
                            title : 'Access denied',
                            content : 'Access denied. Redirecting to start page.',
                            placement : 'top-right',
                            type : 'warning',
                            show : true,
                            duration : 10
                        });
                        $location.path('/');
                        return $q.reject(response);
                    }, function() {
                        // User not logged in or session is expired; redirect to login page.
                        //console.log('User not logged in or session is expired; redirect to login page.');
                        $rootScope.alert({
                            title : 'Access denied',
                            content : 'You are not logged in or your session has expired. Please log in.',
                            placement : 'top-right',
                            type : 'info',
                            show : true,
                            duration : 5
                        });
                        _logout();
                        //$location.path('/login');
                        $rootScope.showLoginModal(method === 'GET');
                        return $q.reject(response);
                    });
                case 400: // bad request
                case 500: // internal server error
                    $rootScope.logErrorResponse(response);
                    $rootScope.alert({
                        title : _.get(response, 'data.error', 'Error'),
                        content : _.get(response, 'data.message', 'An error occured.'),
                        placement : 'top-right',
                        type : 'danger',
                        show : true,
                        duration : 15
                    });
                    return $q.reject(response);
                default:
                    return $q.reject(response);
                }
            }
        };
    }]);

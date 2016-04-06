/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular) {
'use strict';

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
                console.log('Location: ' + $location.path());
                var url = _.get(response, 'config.url', '').trim();
                console.log('url: ' + url);

                switch(response.status) {
                case 302:
                case 403:
                    if (url === 'user') {
                        //console.log('Error fetching user data.');
                        $rootScope.logErrorResponse(response);
                        return $q.reject(response);
                    }
                    $rootScope.heartbeat().then(function(data) {
                        // User is logged in, but denied access; redirect to start page.
                        //console.log('User is logged in, but denied access; redirect to start page.');
                        $rootScope.alert({
                            title : 'Access denied',
                            content : 'Access denied. Redirecting to start page.',
                            placement : 'top-right',
                            type : 'warning',
                            show : true,
                            duration : 15
                        });
                        $location.path('/');
                        return $q.reject(response);
                    }, function(error) {
                        // User not logged in or session is expired; redirect to login page.
                        //console.log('User not logged in or session is expired; redirect to login page.');
                        $rootScope.alert({
                            title : 'Access denied',
                            content : 'You are not logged in or your session has expired. Please log in.',
                            placement : 'top-right',
                            type : 'info',
                            show : true,
                            duration : 10
                        });
                        _logout();
                        $location.path('/login');
                        return $q.reject(response);
                    });
                case 400:
                    // bad request
                case 500:
                    // internal server error
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
                    return response;
                }
            }
        };
    }]);

})(console, _, angular);
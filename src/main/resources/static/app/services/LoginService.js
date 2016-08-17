/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(jQuery, _, angular) {
    'use strict';

    angular.module('ProcessApp.services')
        .factory('LoginService', [ 
                                   '$rootScope',
                                   '$cookies',
                                   '$http',
                                   '$q',
                                   '$alert',
                                   '$modal',
            function (
                    $rootScope,
                    $cookies,
                    $http,
                    $q,
                    $alert,
                    $modal
                    ) {

                var service = {};

                var _serialiseRoles = function(roles) {
                    if (!jQuery.isArray(roles)) { 
                        return ''; 
                    } else {
                        return roles.join(',');
                    }
                };

                var _storeUserdata = function(userdata) {
                    $cookies.put('userid', userdata.userid);
                    $cookies.put('username', userdata.username);
                    $cookies.put('roles', _serialiseRoles(userdata.roles));
                };

                var authenticate = function() {
                    var deferred = $q.defer();
                    $http.get('user').success(function(data) {
                        if (data.username) {
                            var userdata = {
                                userid: ''+data.id,
                                username: data.username,
                                roles: [],
                                features: [],
                                lab: null
                            };
                            if (data.roles) {
                                for(var i in data.roles) {
                                    userdata.roles.push(data.roles[i].name);
                                }
                            }
                            _storeUserdata(userdata);
                            $rootScope.updateUserData(userdata);
                            deferred.resolve();
                        } else {
                            $rootScope.authenticated = false;
                            deferred.reject();
                        }
                    }).error(function() {
                        $rootScope.authenticated = false;
                        deferred.reject();
                    });
                    return deferred.promise;
                };

                var _refreshCookie = function() {
                    return $http.get('/ping');
                };

                var _error = function(response) {
                    var message = _.get(response, 'data.message', '');
                    $rootScope.error = true;
                    $rootScope.authenticated = false;
                    $rootScope.errormessage = message;
                };

                service.login = function(credentials) {
                    var deferred = $q.defer();
                    _refreshCookie()
                    .then(function() {
                        $http.post('login', jQuery.param(credentials), {
                            headers : {
                                'content-type' : 'application/x-www-form-urlencoded'
                            }
                        }).success(function() {
                            authenticate().then(function() {
                                if ($rootScope.authenticated) {
                                    $rootScope.error = false;
                                    $rootScope.errormessage = '';
                                    deferred.resolve();
                                } else {
                                    _error();
                                    deferred.reject();
                                }
                            }, function() {
                                _error();
                                deferred.reject();
                            });
                        }).error(function(response) {
                            _error(response);
                            deferred.reject(response);
                        });
                    }, function(response) {
                        _error(response);
                        deferred.reject(response);
                    });
                    return deferred.promise;
                };

                service.showLogin = function(reloadPageAfterLogin) {
                    $rootScope.reloadPageAfterLogin = reloadPageAfterLogin;
                    service.loginModal = $modal({
                        id: 'loginWindow',
                        scope: $rootScope,
                        templateUrl: '/app/login/login-modal.html',
                        backdrop: 'static'
                    });

                };

                service.hideLogin = function() {
                    if (service.loginModal) {
                        service.loginModal.hide();
                    }
                };

                return service;
            }]);
})(jQuery, _, angular);

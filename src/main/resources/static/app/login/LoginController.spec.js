/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
describe('LoginController', function() {

    'use strict';

    beforeEach(module('ProcessApp.controllers', 'ngCookies'));

    var $httpBackend, $rootScope, $controller, $cookies, pingRequestHandler, loginRequestHandler, userRequestHandler;
    // raw user data
    var _rawUserData = {
        "currentRole":"palga",
        "roles":[
            {
                "id":3,
                "name":"palga"
            }
        ],
        "active":true,
        "id":17,
        "username":"palga@dntp.thehyve.nl",
        "password1":null,
        "password2":null,
        "firstName":"palga",
        "lastName":"",
        "emailValidated":true,
        "contactData":{
            "id":18,
            "telephone":null,
            "email":"palga@dntp.thehyve.nl",
            "address1":null,
            "address2":null,
            "postalCode":null,
            "city":null,
            "stateProvince":null,
            "country":"NL"
        },
        "labId":8,
        "hubLabIds":[

        ],
        "institute":null,
        "specialism":null,
        "created":1459780449106,
        "createdTime":1459780449106,
        "pathologist":false
    };
    // after being trimmed
    var _trimmedUserData = {userid: '17', username: 'palga@dntp.thehyve.nl', roles: ['palga'], features: [], lab: null};

    beforeEach(inject(function(_$controller_, $injector, _$rootScope_, _$cookies_) {

        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');

        // backend definition common for all tests
        pingRequestHandler = $httpBackend.when('GET', '/ping')
            .respond();
        loginRequestHandler = $httpBackend.when('POST', 'login')
            .respond();
        userRequestHandler = $httpBackend.when('GET', 'user')
            .respond(_rawUserData);

        $rootScope = _$rootScope_;
        $rootScope.authenticated = false;
        $rootScope.error = false;
        $rootScope.updateUserData = function(value) {};

        // start spying $rootScope
        spyOn($rootScope, 'updateUserData');

        $cookies = _$cookies_;
        $cookies.put = function (k,v) {};
        // start spying $rootScope
        spyOn($cookies, 'put');

        // The injector unwraps the underscores (_) from around the parameter
        // names when matching
        $controller = _$controller_;
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('$scope.login', function() {

        var $scope, controller;

        beforeEach(function() {
            $scope = {};
            controller = $controller('LoginController', { $scope: $scope });
        });

        it ('should update user with trimmed data', function () {
            $scope.login();
            $httpBackend.flush();

            expect($cookies.put).toHaveBeenCalledWith('userid', _trimmedUserData.userid);
            expect($cookies.put).toHaveBeenCalledWith('username', _trimmedUserData.username);
            expect($cookies.put).toHaveBeenCalledWith('roles', _trimmedUserData.roles.join(','));

            expect($rootScope.updateUserData).toHaveBeenCalledWith(_trimmedUserData);
        });

        it ('should fail authentication', function () {

            userRequestHandler.respond(401, '');

            $scope.login();
            $httpBackend.flush();

            expect($rootScope.error).toBe(true);
        });

    });

});

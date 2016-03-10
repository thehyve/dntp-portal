/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
describe('LoginController', function() {
    'use strict';

    beforeEach(module('ProcessApp.controllers', 'ngCookies'));

    var $controller;

    beforeEach(inject(function(_$controller_) {
        // The injector unwraps the underscores (_) from around the parameter
        // names when matching
        $controller = _$controller_;
    }));

    describe('Login Page', function() {
        var $scope, controller;

        beforeEach(function() {
            controller = $controller('LoginController', {
                $scope : $scope
            });
        });

        // TODO

    });

});

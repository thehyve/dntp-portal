/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
describe('AdminLabController', function() {

    'use strict';

    var $httpBackend, $controller, $scope, $modal, $alert, Lab, adminLabsReqHandler;

    beforeEach(module('ProcessApp.controllers'));

    beforeEach(inject(function(_$controller_, _$rootScope_) {

        // Set up the mock http service responses
        $httpBackend = $injector.get('$httpBackend');

        // Mock the data
        adminLabsReqHandler = $httpBackend.when('GET', '/admin/labs')
            .respond();

        // The injector unwraps the underscores (_) from around the parameter
        // names when matching
        $scope = _$rootScope_;
        $modal = {};
        $alert = {};
        Lab = {
            query: function (){return }
        };
        spyOn (Lab, 'query');

        $controller = _$controller_('AdminLabController', {
            $scope: $scope,
            $modal: $modal,
            $alert: $alert,
            Lab :Lab
        });

    }));

    it ('should be defined', function () {
        expect($controller).toBeDefined();
    })

});

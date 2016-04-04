/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

(function() {
    'use strict';

    describe('service UploadService', function() {
        var Upload, $http, $alert, FlowOptionService, $cookies;

        beforeEach(module('ProcessApp.services'));

        beforeEach(inject(function(_Upload_, _$http_, _$alert_, _$cookies_) {
            Upload = _Upload_;
            $http = _$http_;
            $alert = _$alert_;
            FlowOptionService = _FlowOptionService_; // TODO mock with dependencies
        }));

        it('should be registered', function() {
            console.log('im here')
            expect(Upload).not.toEqual(null);
        });

    });
})();

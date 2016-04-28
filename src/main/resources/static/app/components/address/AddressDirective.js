/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('contactDataAddress', function() {
        'use strict';

        return {
            restrict: 'E',
            scope: {
                contactData: '='
            },
            templateUrl: 'app/components/address/address-template.html'
        };
    });

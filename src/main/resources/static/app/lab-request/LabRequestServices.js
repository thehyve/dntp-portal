/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(angular) {
    'use strict';

    var LabRequestFactory = function($resource) {
        var _labRequestFactory = $resource('/labrequests/:id', {
            id : '@id'
        }, {
            getDetailed : {
                url : '/labrequests/detailed',
                method : 'GET',
                isArray: true
            },
            approve : {
                url : '/labrequests/:id/approve',
                method : 'PUT'
            },
            reject : {
                url : '/labrequests/:id/reject',
                method : 'PUT'
            },
            sending : {
                url : '/labrequests/:id/sending',
                method : 'PUT'
            },
            received : {
                url : '/labrequests/:id/received',
                method : 'PUT'
            },
            returning : {
                url : '/labrequests/:id/returning',
                method : 'PUT'
            },
            completeReturned : {
                url : '/labrequests/:id/completereturned',
                method : 'PUT'
            },
            completeReportsOnly : {
                url : '/labrequests/:id/completereportsonly',
                method : 'PUT'
            },
            completeRejected : {
                url : '/labrequests/:id/completerejected',
                method : 'PUT'
            },
            claim : {
                url : '/labrequests/:id/claim',
                method : 'PUT'
            },
            unclaim : {
                url : '/labrequests/:id/unclaim',
                method : 'PUT'
            }
        });

        _labRequestFactory.statuses = [
            'Waiting for lab approval',
            'Approved',
            'Sending',
            'Received',
            'Returning',
            'Returned',
            'Rejected',
            'Completed'
        ];

        return _labRequestFactory;
    };

    LabRequestFactory.$inject = [ '$resource' ];
    angular.module('ProcessApp.services').factory('LabRequest',
            LabRequestFactory);

    var LabRequestCommentFactory = function($resource) {
        return $resource('/labrequests/:labRequestId/comments/:id', {
            labRequestId : '@labRequestId',
            id : '@id'
        }, {
            update : {
                method : 'PUT'
            },
            remove : {
                method : 'DELETE'
            }
        });
    };
    LabRequestCommentFactory.$inject = [ '$resource' ];
    angular.module('ProcessApp.services').factory('LabRequestComment',
            LabRequestCommentFactory);
  
  
})(angular);

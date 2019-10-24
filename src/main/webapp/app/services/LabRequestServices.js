/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

var LabRequestFactory = function($resource) {
    var _labRequestFactory = $resource('/api/labrequests/:id', {
        id : '@id'
    }, {
        getDetailed : {
            url : '/api/labrequests/detailed',
            method : 'GET',
            isArray: true
        },
        approve : {
            url : '/api/labrequests/:id/approve',
            method : 'PUT'
        },
        reject : {
            url : '/api/labrequests/:id/reject',
            method : 'PUT'
        },
        sending : {
            url : '/api/labrequests/:id/sending',
            method : 'PUT'
        },
        received : {
            url : '/api/labrequests/:id/received',
            method : 'PUT'
        },
        returning : {
            url : '/api/labrequests/:id/returning',
            method : 'PUT'
        },
        completeReturned : {
            url : '/api/labrequests/:id/completereturned',
            method : 'PUT'
        },
        completeReportsOnly : {
            url : '/api/labrequests/:id/completereportsonly',
            method : 'PUT'
        },
        completeRejected : {
            url : '/api/labrequests/:id/completerejected',
            method : 'PUT'
        },
        claim : {
            url : '/api/labrequests/:id/claim',
            method : 'PUT'
        },
        unclaim : {
            url : '/api/labrequests/:id/unclaim',
            method : 'PUT'
        },
        undoapprove : {
            url : '/api/labrequests/:id/undoapprove',
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
    return $resource('/api/labrequests/:labRequestId/comments/:id', {
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

/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

var RequestFactory = function($resource) {

    var _requestFactory = $resource('/api/requests/:id', {
        id : '@processInstanceId'
    }, {
        update : {
            method : 'PUT'
        },
        reopen : {
            url : '/api/requests/:id/reopen',
            method : 'PUT'
        },
        fork : {
            url : '/api/requests/:id/forks',
            method : 'POST'
        },
        submit : {
            url : '/api/requests/:id/submit',
            method : 'PUT'
        },
        submitReview : {
            url : '/api/requests/:id/submitReview',
            method : 'PUT'
        },
        finalise : {
            url : '/api/requests/:id/finalise',
            method : 'PUT'
        },
        close : {
            url : '/api/requests/:id/close',
            method : 'PUT'
        },
        reject : {
            url : '/api/requests/:id/reject',
            method : 'PUT'
        },
        remove : {
            method : 'DELETE'
        },
        claim : {
            url : '/api/requests/:id/claim',
            method : 'PUT'
        },
        unclaim : {
            url : '/api/requests/:id/unclaim',
            method : 'PUT'
        },
        suspend : {
            url : '/api/requests/:id/suspend',
            method : 'PUT'
        },
        resume : {
            url : '/api/requests/:id/resume',
            method : 'PUT'
        },
        selectAll : {
            url : '/api/requests/:id/selectAll',
            method : 'PUT'
        },
        useExampleExcerptList : {
            url : '/api/requests/:id/excerptList/useExample',
            method : 'POST'
        },
        submitExcerptSelection : {
            url : '/api/requests/:id/submitExcerptSelection',
            method : 'PUT'
        },
        updateExcerptSelectionApproval : {
            url : '/api/requests/:id/excerptSelectionApproval',
            method : 'PUT'
        }
    });

    _requestFactory.privacyCommitteeRationaleOptions = [
        'ppc_handled_according_mandate',
        'ppc_handled_according_mandate_explanation',
        'ppc_approved_written_procedure',
        'ppc_discuss',
        'ppc_rationale_exploratory_request',
        'ppc_local_request'
    ];

    _requestFactory.convertRequestNumber = function (request) {
        var _number = '' + request.requestNumber;
        if (_number.length > 5) {
            var parts = _number.split('-');
            if (parts.length > 1) {
                var value = Number(parts[0])*1000000000;
                value += Number(parts[1]*1000);
                if (parts.length > 2) {
                    value += Number(parts[2].substring(1, parts[2].length));
                }
                return value;
            }
            return -1;
        }
        return -1;
    };

    _requestFactory.convertLabRequestCode = function (labrequest) {
        var _number = '' + labrequest.labRequestCode;
        var parts = _number.split('-');
        if (parts.length >= 3) {
            var value = Number(parts[0])*1000*1000*1000*1000;
            value += Number(parts[1]*1000*1000);
            if (parts.length >= 4) {
                value += Number(parts[2].substring(1, parts[2].length)) * 1000;
                value += Number(parts[3]);
            } else {
                value += Number(parts[2]);
            }
            return value;
        }
        return -1;
    };

    _requestFactory.isMaterialsRequest = function (request) {
        return request.blockMaterialsRequest ||
            request.heSliceMaterialsRequest ||
            request.otherMaterialsRequest
    };

    _requestFactory.statuses = [
        'Open',
        'Review',
        'Approval',
        'DataDelivery',
        'SelectionReview',
        'LabRequest',
        'Rejected',
        'Closed'
    ];

    _requestFactory.displayStatuses = [
        'Open',
        'Review',
        'Approval',
        'Approved, waiting for data',
        'Data delivered',
        'Data delivered, select excerpts',
        'SelectionReview',
        'LabRequest',
        'Rejected',
        'Closed'
    ];

    _requestFactory.claimableStates = _.difference(_requestFactory.statuses,
            [
                 'Open',
                 'LabRequest',
                 'Rejected',
                 'Closed'
            ]);

    _requestFactory.editStates = [
        'Open',
        'Review',
        'Approval'
    ];

    _requestFactory.statusesForRole = {
        'palga': _requestFactory.displayStatuses,
        'requester': _requestFactory.displayStatuses,
        'scientific_council': _.difference(_requestFactory.displayStatuses,
                ['Open', 'Review']),
        'lab_user': ['LabRequest', 'Rejected', 'Closed'],
        'hub_user': ['LabRequest', 'Rejected', 'Closed']
    };

    _requestFactory.getStatusesForRole = function (role) {
        return _.get(_requestFactory.statusesForRole, role, []);
    };

    return _requestFactory;
};
RequestFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('Request', RequestFactory);

var RequestAttachmentFactory = function($resource) {
    return $resource('/api/requests/:requestId/files/:id', {
        requestId: '@requestId',
        id : '@id'
    }, {
        remove : {
            method : 'DELETE'
        },
        removeInformedConsentFormFile : {
            url : '/api/requests/:requestId/informedConsentFormFiles/:id',
            method : 'DELETE'
        },
        removeDataFile : {
            url : '/api/requests/:requestId/dataFiles/:id',
            method : 'DELETE'
        },
        removeAgreementFile : {
            url : '/api/requests/:requestId/agreementFiles/:id',
            method : 'DELETE'
        },
        removeMECFile : {
            url : '/api/requests/:requestId/mecFiles/:id',
            method : 'DELETE'
        }
    });
};

RequestAttachmentFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('RequestAttachment', RequestAttachmentFactory);

var RequestCommentFactory = function($resource) {
    return $resource('/api/requests/:processInstanceId/comments/:id', {
        processInstanceId: '@processInstanceId',
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
RequestCommentFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('RequestComment', RequestCommentFactory);

var ApprovalCommentFactory = function($resource) {
    return $resource('/api/requests/:processInstanceId/approvalComments/:id', {
        processInstanceId: '@processInstanceId',
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
ApprovalCommentFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('ApprovalComment', ApprovalCommentFactory);

var ApprovalVoteFactory = function($resource) {
    return $resource('/api/requests/:processInstanceId/approvalVotes/:id', {
        processInstanceId: '@processInstanceId',
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
ApprovalVoteFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('ApprovalVote', ApprovalVoteFactory);

var ExcerptListFactory = function($resource) {
    return $resource('/api/requests/:processInstanceId/selection', {
        processInstanceId: '@processInstanceId'
    }, {
    });
};
ExcerptListFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('ExcerptList', ExcerptListFactory);

var ExcerptEntryFactory = function($resource) {
    return $resource('/api/requests/:processInstanceId/excerpts/:id/selection', {
        processInstanceId: '@processInstanceId',
        id : '@id'
    }, {
        update : {
            method : 'PUT'
        }
    });
};
ExcerptEntryFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('ExcerptEntry', ExcerptEntryFactory);

var FormDataFactory = function($resource) {
    return $resource('/formdata/:id', {
        id : '@id'
    }, {
        update : {
            method : 'PUT'
        }
    });
};
FormDataFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('FormData', FormDataFactory);

var FlowOptionService = function($cookies) {
    return {
        get_default: function(options) {
            /*eslint-disable no-unused-vars*/
            options.headers = function (file, chunk, isTest) {
                var csrftoken = $cookies.get('XSRF-TOKEN');
                return {
                    'X-CSRFToken': csrftoken,
                    'X-XSRF-TOKEN': csrftoken
                };
            };
            /*eslint-enable no-unused-vars*/
            options.testChunks = false;
            options.forceChunkSize = true;
            return options;
        }
    };
};
FlowOptionService.$inject = [ '$cookies' ];
angular.module('ProcessApp.services').factory('FlowOptionService', FlowOptionService);

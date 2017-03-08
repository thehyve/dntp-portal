/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, angular, _) {
    'use strict';

    var RequestFactory = function($resource) {

        var _requestFactory = $resource('/requests/:id', {
            id : '@processInstanceId'
        }, {
            update : {
                method : 'PUT'
            },
            reopen : {
                url : '/requests/:id/reopen',
                method : 'PUT'
            },
            fork : {
                url : '/requests/:id/forks',
                method : 'POST'
            },
            submit : {
                url : '/requests/:id/submit',
                method : 'PUT'
            },
            submitForApproval : {
                url : '/requests/:id/submitForApproval',
                method : 'PUT'
            },
            finalise : {
                url : '/requests/:id/finalise',
                method : 'PUT'
            },
            close : {
                url : '/requests/:id/close',
                method : 'PUT'
            },
            reject : {
                url : '/requests/:id/reject',
                method : 'PUT'
            },
            remove : {
                method : 'DELETE'
            },
            claim : {
                url : '/requests/:id/claim',
                method : 'PUT'
            },
            unclaim : {
                url : '/requests/:id/unclaim',
                method : 'PUT'
            },
            suspend : {
                url : '/requests/:id/suspend',
                method : 'PUT'
            },
            resume : {
                url : '/requests/:id/resume',
                method : 'PUT'
            },
            selectAll : {
                url : '/requests/:id/selectAll',
                method : 'PUT'
            },
            useExampleExcerptList : {
                url : '/requests/:id/excerptList/useExample',
                method : 'POST'
            },
            submitExcerptSelection : {
                url : '/requests/:id/submitExcerptSelection',
                method : 'PUT'
            },
            updateExcerptSelectionApproval : {
                url : '/requests/:id/excerptSelectionApproval',
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
        return $resource('/requests/:requestId/files/:id', {
            requestId: '@requestId',
            id : '@id'
        }, {
            remove : {
                method : 'DELETE'
            },
            removeDataFile : {
                url : '/requests/:requestId/dataFiles/:id',
                method : 'DELETE'
            },
            removeAgreementFile : {
                url : '/requests/:requestId/agreementFiles/:id',
                method : 'DELETE'
            },
            removeMECFile : {
                url : '/requests/:requestId/mecFiles/:id',
                method : 'DELETE'
            }
        });
    };

    RequestAttachmentFactory.$inject = [ '$resource' ];
    angular.module('ProcessApp.services').factory('RequestAttachment', RequestAttachmentFactory);

    var RequestCommentFactory = function($resource) {
        return $resource('/requests/:processInstanceId/comments/:id', {
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
        return $resource('/requests/:processInstanceId/approvalComments/:id', {
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
        return $resource('/requests/:processInstanceId/approvalVotes/:id', {
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
        return $resource('/requests/:processInstanceId/selection', {
            processInstanceId: '@processInstanceId'
        }, {
        });
    };
    ExcerptListFactory.$inject = [ '$resource' ];
    angular.module('ProcessApp.services').factory('ExcerptList', ExcerptListFactory);

    var ExcerptEntryFactory = function($resource) {
        return $resource('/requests/:processInstanceId/excerpts/:id/selection', {
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

}(console, angular, _));

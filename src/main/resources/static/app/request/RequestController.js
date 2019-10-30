/**
 * Copyright (C) 2016-2019  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function (window, console, angular, jQuery, _, bootbox) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('RequestController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'RequestAttachment', 'RequestComment',
        'ApprovalComment', 'ApprovalVote',
        'Upload', '$routeParams', 'RequestFilter',
        '$alert', '$timeout', '$q',
        '$templateCache', '$http',
        'AgreementFormTemplate',
        'Popover',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, RequestAttachment, RequestComment,
                  ApprovalComment, ApprovalVote,
                  Upload, $routeParams, RequestFilter,
                  $alert, $timeout, $q,
                  $templateCache, $http,
                  AgreementFormTemplate,
                  Popover) {

            $scope.displayStatuses = Request.displayStatuses;

            $scope.claimableStates = Request.claimableStates;

            $scope.editStates = Request.editStates;

            $scope.Upload = Upload;

            $scope.privacyCommitteeRationaleOptions = Request.privacyCommitteeRationaleOptions;

            $scope.serverurl = $location.protocol()+'://'+$location.host() +
                (($location.port()===80 || $location.port()===443) ? '' : ':'+$location.port());

            $rootScope.tempRequest = null;

            $scope.getRequest = function() {
                if (!$scope.allRequests) {
                    $scope.allRequests = [];
                }
                Request.get({id:$routeParams.requestId}, function (req) {
                    //req.type = Request.convertRequestOptsToType(req);
                    // set date -- to be used in the agreement form
                    var now = new Date();
                    req.date = now.getDate() + '-' + now.getMonth() + '-' + now.getFullYear();
                    $scope.request = req;
                    $rootScope.tempRequest = jQuery.extend( true, {}, req ); // deep copy
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };

            $scope.getStatusText = function(request) {
                if (request.status == 'DataDelivery') {
                    if ((request.statisticsRequest && request.dataAttachmentCount > 0) ||
                        (request.excerptListUploaded && !(request.paReportRequest ||
                            Request.isMaterialsRequest(request) || request.clinicalDataRequest))) {
                        return 'Data delivered';
                    } else if (request.excerptListUploaded && (request.paReportRequest ||
                        Request.isMaterialsRequest(request) || request.clinicalDataRequest)) {
                        return 'Data delivered, select excerpts';
                    } else {
                        return 'Approved, waiting for data';
                    }
                } else {
                    return request.status;
                }
            };

            $scope.getRequests = function() {
                Request.query().$promise.then(function(response) {
                    $scope.allRequests = response ? response : [];
                    $scope.allRequests.forEach(function(req) {
                        req.number = Request.convertRequestNumber(req);
                        req.statusText = $scope.getStatusText(req);
                        if (req.approvalVote == null) {
                            req.approvalVote = '';
                        }
                        //req.type = Request.convertRequestOptsToType(req);
                    });
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };

            $scope.selections = {
                overview: RequestFilter.selectAll,
                suspended: RequestFilter.selectSuspended,
                claimed: RequestFilter.selectClaimed($rootScope.currentUserId),
                unclaimed: RequestFilter.selectUnclaimed,
                voted: RequestFilter.selectVoted,
                notvoted: RequestFilter.selectNotVoted
            };

            _(Request.getStatusesForRole($scope.currentRole)).forEach(function(status) {
                $scope.selections[status] = RequestFilter.selectByStatus(status);
            });

            $scope.isStatusPage = function() {
                return _.includes(Request.displayStatuses, $scope.activeSidebar);
            };

        $scope.checkTableFilterStatus = function() {
		// Table filter status
		var tfs = $scope.activeSidebar;
		$scope.tableFilterStatus = tfs;
		if(!$scope.isStatusPage()) {
                    $scope.tableFilterStatus = "";
                }
		// Apply the scope to trigger correct initialization of persisted local storage for smart table
		$scope.$apply();
        };

        // Used in the stPersistedSearch directive
        $scope.persistKey = 'requests';

        $scope.resetFilters = function(key) {
            localStorage.setItem(key, JSON.stringify({}));
            $route.reload();
        };

        $scope.showSelection = function(requests) {
            var selection = $scope.activeSidebar;

            if (requests && selection in $scope.selections) {
                $scope.requests = $scope.selections[selection](requests);
            } else {
                $scope.requests = [];
            }
            $scope.displayedCollection = [].concat($scope.requests);
        };

        $scope.$watch('allRequests', function(newValue) {
            if (newValue) {
                $scope.showSelection(newValue);
                $timeout( function() {
                    $scope.checkTableFilterStatus();
                    // Removing the statusText from the filter we store in localStorage. This to prevent
                    // the stored filter from overwriting the sidebar button people clicked on.
                    if($scope.isStatusPage()){
                        $scope.tableFilterStatus = "";
                        var table_state = JSON.parse(localStorage.getItem($scope.persistKey));
                        if('search' in table_state && 'statusText' in table_state['search']['predicateObject']) {
                            delete table_state['search']['predicateObject']['statusText'];
                            localStorage.setItem($scope.persistKey, JSON.stringify(table_state));
                        }
                    }
                },0);
            }
        });

        if ($routeParams.requestId) {
            $scope.getRequest();
        } else {
            if ($routeParams.selection && $routeParams.selection in $scope.selections) {
                $scope.activeSidebar = $routeParams.selection;
            } else {
                $scope.activeSidebar = 'overview';
            }
            $scope.getRequests();
        }

        $scope.loadTemplate = function() {
            AgreementFormTemplate.get()
            .then(function (template) {
                $scope.agreementFormTemplate = template;
            }, function (response) {
                $rootScope.logErrorResponse(response);
            });
        };

        $scope.printAgreementForm = function () {
            window.print();
        };

        if ($route.current.templateUrl === 'app/request/agreementform.html') {
            $scope.loadTemplate();
            AgreementFormTemplate.replaceVariables($scope, 'agreementFormTemplate.contents', 'request', 'template_contents');
        }

            $scope.resetDataLinkageValues = function (request, isOnlyResetReason) {
                if (!isOnlyResetReason) {
                    request.linkageWithPersonalDataNotes = '';
                    request.informedConsent = false;
                }
                request.reasonUsingPersonalData = '';
            };

        $scope.resetPreviousContactValues = function (request) {
            request.previousContactDescription = '';
        };

        $scope.isValidRequestType = function(request) {
            return (request.statisticsRequest ||
                    request.excerptsRequest ||
                    request.paReportRequest ||
                    Request.isMaterialsRequest(request) ||
                    request.clinicalDataRequest);
        };

        $scope.resetNonStatistcsRequestType = function(request) {
            this.otherMaterialsRequestSelected = false;
            request.excerptsRequest = false;
            request.paReportRequest = false;
            request.blockMaterialsRequest = false;
            request.heSliceMaterialsRequest = false;
            request.otherMaterialsRequest = '';
            request.clinicalDataRequest = false;
        };

        $scope.resetOtherMaterialsRequest = function(request) {
            if (!this.otherMaterialsRequestSelected) {
                request.otherMaterialsRequest = '';
            }
        };

            $scope.upload_result = {
                    'attachment': '',
                    'agreement': '',
                    'mec_approval': '',
                    'excerpt_list': '',
                    'excerpt_selection': '',
                    'data': '',
                    'informed_consent_form': ''
            };

        $scope.uploading = false;
        $scope.upload_error = {};

        $scope.fileuploadsubmitted = function(type) {
            $scope.uploading = true;
            $scope.upload_result[type] = '';
            if (type in $scope.upload_error) {
                delete $scope.upload_error[type];
            }
        };

        $scope.fileuploadsuccess = function(request, data, type, flow) {
            $scope.uploading = false;
            $scope.lastUploadedFileName = flow.files[flow.files.length-1].name;
            $scope.upload_result[type] = 'success';
            $scope.upload_error[type] = '';
            $scope.$apply();
            var result = new Request(JSON.parse(data));
            //$scope.refresh(request, result);
            request.attachments = result.attachments;
            request.informedConsentFormAttachments = result.informedConsentFormAttachments;
            request.agreementAttachments = result.agreementAttachments;
            request.excerptList = result.excerptList;
            request.dataAttachments = result.dataAttachments;
            request.dataAttachmentCount = result.dataAttachments.length;
            request.medicalEthicalCommitteeApprovalAttachments = result.medicalEthicalCommitteeApprovalAttachments;
            request.informedConsentFormAttachments = result.informedConsentFormAttachments;
        };

        $scope.disableAgreementReached = function(request) {
            if (request.agreementNotApplicable) {
                request.agreementReached = false;
            }
        };

        $scope.EXCERPT_SELECTION_LIMIT = 1000;

        $scope.excerptCountExceedsSelectionLimit = function(count) {
            return count > $scope.EXCERPT_SELECTION_LIMIT;
        };

        $scope.excerptlistuploadsuccess = function(request, data, type, flow) {
            var entryCount = Number(data);
            $scope.uploading = false;
            $scope.lastUploadedFileName = flow.files[flow.files.length-1].name;
            $scope.upload_result[type] = 'success';
            $scope.upload_error[type] = '';
            $scope.$apply();
            request.excerptListUploaded = true;
            request.excerptList = {entryCount: entryCount};
            if ($scope.excerptCountExceedsSelectionLimit(entryCount)) {
                bootbox.alert(
                    $rootScope.translate('The excerpt list exceeds the limit of ?. The requester cannot use the excerpt selection interface for this request.',
                            {limit: $scope.EXCERPT_SELECTION_LIMIT}));
            }
        };

        $scope.excerptselectionuploadsuccess = function(request, data, type, flow) {
            $scope.uploading = false;
            $scope.lastUploadedFileName = flow.files[flow.files.length-1].name;
            $scope.upload_result[type] = 'success';
            $scope.upload_error[type] = '';
            $scope.$apply();
            request.excerptList.selectedCount = data;
        };

        $scope.fileuploaderror = function(message, type) {
            $scope.uploading = false;
            console.log('Upload error: ' + message);
            $scope.upload_result[type] = 'error';
            $scope.upload_error[type] = message;
        };

        $scope.start = function() {
            $scope.dataLoading = true;
            new Request().$save(function(request) {
                $scope.edit(request);
            }, function(response) {
                $rootScope.logErrorResponse(response);
                $scope.dataLoading = false;
            });
        };

        /*eslint-disable no-unused-vars*/
        $scope.filterEmptyRequests = function(value, index) {
            return !value;
        };
        /*eslint-enable no-unused-vars*/

        $scope.refresh = function(request, result) {
            //result.type = Request.convertRequestOptsToType(result);
            var index = -1;
            for (var i in $scope.allRequests) {
                if ($scope.allRequests[i].processInstanceId === request.processInstanceId) {
                    index = i;
                    break;
                }
            }
            $scope.allRequests[index] = result;
            $route.reload();
            $scope.request = result;
        };

        $scope.update = function(request) {
            $scope.dataLoading = true;
            //Request.convertRequestTypeToOpts(request); // convert request type
            request.$update(function(result) {
                $scope.refresh(request, result);
                $scope.editRequestModal.hide();
                $scope.dataLoading = false;
            }, function(response) {
                $rootScope.logErrorResponse(response);
                $scope.dataLoading = false;
            });
        };

        $scope.removeInformedConsentFormFile = function(f) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete file ?', {name: f.name}), function(result) {
                if (result) {
                    var attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$removeInformedConsentFormFile(function() {
                        $scope.request.informedConsentFormAttachments.splice($scope.request.informedConsentFormAttachments.indexOf(f), 1);
                        bootbox.alert('File ' + f.name + ' deleted.');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };


        $scope.removeAgreementFile = function(f) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete file ?', {name: f.name}), function(result) {
                if (result) {
                    var attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$removeAgreementFile(function() {
                        $scope.request.agreementAttachments.splice($scope.request.agreementAttachments.indexOf(f), 1);
                        bootbox.alert('File ' + f.name + ' deleted.');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };

        $scope.removeMECFile = function(f) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete file ?', {name: f.name}), function(result) {
                if (result) {
                    var attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$removeMECFile(function() {
                        $scope.request.medicalEthicalCommitteeApprovalAttachments.splice($scope.request.medicalEthicalCommitteeApprovalAttachments.indexOf(f), 1);
                        bootbox.alert('File ' + f.name + ' deleted.');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };

        $scope.removeDataFile = function(f) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete file ?', {name: f.name}), function(result) {
                if (result) {
                    var attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$removeDataFile(function() {
                        $scope.request.dataAttachments.splice($scope.request.dataAttachments.indexOf(f), 1);
                        bootbox.alert('File ' + f.name + ' deleted.');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };

        $scope.removeFile = function(f) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete file ?', {name: f.name}), function(result) {
                if (result) {
                    var attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$remove(function() {
                        $scope.request.attachments.splice($scope.request.attachments.indexOf(f), 1);
                        bootbox.alert('File ' + f.name + ' deleted.');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };

        $scope.remove = function(request) {
            bootbox.confirm($rootScope.translate('Are you sure you want to delete request ?', {id: request.requestNumber}), function(choice) {
                if (choice) {
                    request.$remove(function() {
                        $scope.allRequests.splice($scope.allRequests.indexOf(request), 1);
                        bootbox.alert('Request ' + request.processInstanceId + ' deleted.');
                        $location.path('/');
                    }, function(response) {
                        $rootScope.logErrorResponse(response);
                    });
                }
            });
        };

        /**
         * @param version_string e.g., 'dntp_request_002:1:4'
         * @param min_version e.g., dntp_request_002
         */
        var _checkVersionAtLeast = function(version_string, min_version) {
            var parts = version_string.split(':');
            if (parts.length != 3) {
                return false;
            }
            var current_version = parts[0];
            var result = current_version >= min_version;
            return result;
        };

        $scope.isReopenEnabled = function(request) {
            return _checkVersionAtLeast(request.processId, 'dntp_request_002');
        };

        $scope.reopen = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to reopen the request? ' +
                    'After reopening the request will be editable by the requester.'),
                function(confirmed) {
                    if (confirmed) {
                        //Request.convertRequestTypeToOpts(request); // convert request type
                        request.$reopen(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.isForkEnabled = function(request) {
            return _checkVersionAtLeast(request.processId, 'dntp_request_003');
        };

        $scope.isClinicalDataEnabled = function(request) {
            return _checkVersionAtLeast(request.processId, 'dntp_request_003');
        };

        $scope.fork = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to create an additional request?'),
                function(confirmed) {
                    if (confirmed) {
                        //Request.convertRequestTypeToOpts(request); // convert request type
                        request.$fork(function(result) {
                            $scope.view(result);
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.submitRequest = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to submit the request? ' +
                    'After submission the request cannot be edited anymore.'),
                function(confirmed) {
                    if (confirmed) {
                        //Request.convertRequestTypeToOpts(request); // convert request type
                        request.$submit(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $scope.dataLoading = false;
                            $rootScope.logErrorResponse(response);
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.submitForApproval = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                    $rootScope.translate('Are you sure you want to send the request to the scientific council?'),
                function(confirmed) {
                    if (confirmed) {
                        request.$submitReview(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.isSkipApprovalEnabled = function(request) {
            return _checkVersionAtLeast(request.processId, 'dntp_request_005');
        };

        $scope.submitReviewAndSkipApproval = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to finish the submission process ' +
                    'and skip the scientific council for this request?'),
                function(confirmed) {
                    if (confirmed) {
                        request.skipStatusApproval = true;
                        request.$submitReview(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.finalise = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                    $rootScope.translate('Are you sure you want to finalise the request?'),
                function(confirmed) {
                    if (confirmed) {
                        request.$finalise(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                            $scope.dataLoading = false;
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.closeRequest = function(request) {
            $scope.dataLoading = true;
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to close the request?<br>' +
                        'After closing, no data files can be added.'),
                function(confirmed) {
                    if (confirmed) {
                        request.$close(function(result) {
                            $scope.refresh(request, result);
                            //$scope.editRequestModal.hide();
                        }, function(response) {
                            $scope.dataLoading = false;
                            $rootScope.logErrorResponse(response);
                        });
                    } else {
                        $scope.dataLoading = false;
                        $scope.$apply();
                    }
                });
        };

        $scope.reject = function(request) {
            bootbox.confirm(
                '<h4>' + $rootScope.translate('Are you sure you want to reject the request?') + '</h4>\n' +
                '<form id="reject" action="">' +
                $rootScope.translate('Please enter the reason for rejection.') +
                '\n<br><br>\n' +
                '<textarea type="text" class="form-control" name="rejectReason" id="rejectReason" required autofocus ng-model="rejectReason"></textarea>' +
                '</form>',
                function(result) {
                    if (result) {
                        $scope.dataLoading = true;
                        request.rejectReason = jQuery('#rejectReason').val();
                        request.$reject(function(result) {
                            $scope.refresh(request, result);
                            $scope.editRequestModal.hide();
                            $scope.dataLoading = false;
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    } else {
                        $scope.$apply();
                    }
                }
            );
        };

        $scope.approveSelection = function(request) {
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to approve the selection?<br>' +
                'After approving, lab requests will be generated.'),
                function(confirmed) {
                    if (confirmed) {
                        $scope.dataLoading = true;
                        request.selectionApproved = true;
                        request.$updateExcerptSelectionApproval(function(result) {
                            $scope.refresh(request, result);
                            $scope.dataLoading = false;
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    }
                });
        };

        $scope.rejectSelection = function(request) {
            bootbox.confirm(
                $rootScope.translate('Are you sure you want to reject the selection?<br>' +
                'After rejecting, the status will return to \'Approved, waiting for data.\''),
                function(confirmed) {
                    if (confirmed) {
                        $scope.dataLoading = true;
                        request.selectionApproved = false;
                        request.$updateExcerptSelectionApproval(function(result) {
                            $scope.refresh(request, result);
                            $scope.dataLoading = false;
                        }, function(response) {
                            $rootScope.logErrorResponse(response);
                            $scope.dataLoading = false;
                        });
                    }
                });
        };

        $scope.uploadDataFile = function(flow) {
            var mb_max = 10;
            var max_size = 1024*1024*mb_max;
            if (flow.getSize() > max_size) {
                var mb_size = (flow.getSize()/(1024*1024)).toFixed(1);
                bootbox.alert($rootScope.translate('File too large', {'mb_size': mb_size, 'mb_max': mb_max}));
                flow.cancel();
                $scope.uploading = false;
            } else {
                Upload.uploadFile(flow);
            }
        };

        $scope.selectPANumbers = function(request) {
            $location.path('/request/' + request.processInstanceId + '/selection');
        };

        $scope.view = function(request) {
            $location.path('/request/view/' + request.processInstanceId);
        };

        $scope.cancelByEscKey = function (key, request) {
            //console.log('In cancelByEscKey');
            if (key.keyCode === 27) {
                //console.log('Escape key');
                $scope.cancel(request);
            }
        };

        $scope.cancel = function (request) {
            if ($rootScope.tempRequest.title === null) {
                request.$remove(function (result) {
                    $scope.allRequests.splice($scope.allRequests.indexOf(request), 1);
                    $scope.refresh(request, result);
                }, function (response) {
                    $rootScope.logErrorResponse(response);
                    return;
                });
            } else {
                // return request to the original
                $scope.request = jQuery.extend( true, {},  $rootScope.tempRequest ); // deep copy
            }
            $scope.editRequestModal.hide();
        };

        $scope.edit = function(request) {
            if (request) {
                Request.get({id:request.processInstanceId}, function (data) {
                    //data.type = Request.convertRequestOptsToType(data);
                    $scope.request = data;
                    $rootScope.tempRequest = jQuery.extend( true, {}, data ); // deep copy

                    if ($rootScope.tempRequest.title === null) {
                        $scope.request.type = '1';
                    }

                    if ($scope.globals.currentUser.roles.indexOf('scientific_council') !== -1) {
                        if (!$scope.request.approvalVotes) {
                            $scope.request.approvalVotes = {};
                        }
                        if (!($scope.globals.currentUser.userid in $scope.request.approvalVotes)) {
                            $scope.request.approvalVotes[$scope.globals.currentUser.userid] =
                                new ApprovalVote({value: 'NONE'});
                        }
                    }
                    $scope.otherMaterialsRequestSelected = !!request.otherMaterialsRequest;
                    $scope.editComment = {};
                    $scope.approvalComment = {};
                    $scope.commentEditVisibility = {};
                    if (data.returnDate === null) {
                        data.returnDate = new Date();
                    }
                    $scope.editRequestModal = $modal({
                        id: 'editRequestWindow',
                        scope: $scope,
                        templateUrl: '/app/request/edit-request.html',
                        backdrop: 'static'
                    });
                    $scope.dataLoading = false;
                });
            }
        };

        $scope.open = function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.opened = true;
        };

        $scope.claim = function(request) {
            request.$claim(function(result) {
                $scope.refresh(request, result);
            }, function(response) {
                $rootScope.logErrorResponse(response);
            });
        };

        $scope.unclaim = function(request) {
            request.$unclaim(function(result) {
                $scope.refresh(request, result);
            }, function(response) {
                $rootScope.logErrorResponse(response);
            });
        };

        $scope.suspend = function(request) {
            request.$suspend(function(result) {
                $scope.refresh(request, result);
            }, function(response) {
                $rootScope.logErrorResponse(response);
            });
        };

        $scope.resume = function(request) {
            request.$resume(function(result) {
                $scope.refresh(request, result);
            }, function(response) {
                $rootScope.logErrorResponse(response);
            });
        };

        $scope.size = function(obj) {
            var size = 0, key;
            for (key in obj) {
                if (obj.hasOwnProperty(key)) { size++; }
            }
            return size;
        };

        $scope.isClaimable = function(status) {
            return _.includes($scope.claimableStates, status);
        };

        $scope.excerptSelectionStates = [
            'DataDelivery',
            'SelectionReview',
            'LabRequest'
        ];
        $scope.isExcerptSelectionState = function(state) {
            return _.includes($scope.excerptSelectionStates, state);
        };

        /**
         * Return true iff the curent user is a requester and the status is 'Open'
         * or the current user is a Palga user and the status is one of the
         * statuses where editing is allowed ('Open', 'Review', 'Approval').
         */
        $scope.isEditStatus = function (status) {
            return  ($rootScope.isMyRequest($scope.request)  && status === 'Open') ||
                    ($rootScope.isPalga() && _.includes($scope.editStates, status));
        };

        $scope.autofocus = function() {
            $timeout(function() {
                jQuery('#contactPersonName').focus();
            });
        };

        $scope.popoverEnablers = {
            requestTypePopover: ['statisticsRequestTrue', 'statisticsRequestFalse',
                                 'excerptsRequest', 'paReportRequest',
                                 'blockMaterialsRequest', 'heSliceMaterialsRequest', 'otherMaterialsRequest',
                                 'clinicalDataRequest'],
            dataLinkagePopover: ['linkageWithPersonalDataYes', 'linkageWithPersonalDataNo'],
            informedConsentPopover: ['informedConsentYes', 'informedConsentNo'],
            uploadFilePopover: ['button_upload_attachment'],
            uploadMETCLetterPopover: ['button_upload_metc_letter']
        };

        $scope.showPopover = function(id) {
            Popover.showPopover(id);
        };

        $scope.hidePopover = function(id) {
            Popover.hidePopover(id, $scope.popoverEnablers);
        };

        $scope.enablePopovers = function() {
            Popover.enablePopovers($scope.popoverEnablers);
        };

        $scope.selected_requests = {};
        $scope.filteredCollection = [];

        $scope.toggleSelect = function(filteredCollection) {
            if (_.includes($scope.selected_requests, true)) {
                $scope.selected_requests = {};
            } else {
                $scope.selected_requests = _.fromPairs(
                        _.map(filteredCollection, function(request) {
                            return [request.processInstanceId, true];
                        })
                );
            }
        };

        $scope.print_selection = [];

        var openPrintWindow = function () {
            var _printWindow = window.open('', '_blank');
            return _printWindow;
        };

        var writeToPrintWindow = function (_printWindow) {
            var elementId = 'printcontents';
            var css_links = _.map([
                './css/print.css'
            ], function(link) {
                return '<link rel="stylesheet" type="text/css" href="' + link + '" />';
            }).join('');
            var _contents = '<!DOCTYPE html>' +
                '<html class="printhtml"><head>'
                .concat(css_links)
                .concat('</head><body onload="window.print()">')
                .concat(document.getElementById(elementId).innerHTML)
                .concat('</body></html>');
            _printWindow.document.write(_contents);
            _printWindow.document.close();
        };

        var _templates = [
             'app/components/address/address-template.html',
             'app/request/request-contents.html',
             'app/request/comments.html',
             'app/request/approvals.html',
             'app/request/upload-file.html'
        ];

        /**
         * Prefetch templates for the print preview. If the templates are
         * not prefetched, the templates will be fetched asynchonously,
         * resulting in unrendered parts of the print page.
         *
         * @return promise that fetched the templates when executed.
         */
        var prefetchTemplates = function() {
            var promises = _.map(_templates, function(template) {
                return $q(function(resolve, reject) {
                    $http.get(template).then(function(response) {
                        $templateCache.put(template, response.data);
                        resolve(response);
                    }, function(err) {
                        reject(err);
                    })
                });
            });
            return $q.all(promises);
        };

        /**
         * Fetch the detailed request objects for the selected requests.
         * Reads the selection from <code>$scope.selected_requests</code>.
         * The fetched objects are stored in <code>$scope.print_selection</code>.
         *
         * @return promise that fetches the objects when executed.
         */
        var fetchSelected = function() {
            var selected = _.transform($scope.selected_requests, function(result, value, key) {
                if (value) { result.push(key); }
              }, []);
            $scope.print_selection = [];
            var promises = _.map(selected, function(requestId) {
                return $q(function(resolve, reject) {
                    Request.get({id: requestId}, function(req) {
                        //req.type = Request.convertRequestOptsToType(req);
                        $scope.print_selection.push(req);
                        resolve(req);
                    }, function(response) {
                        reject(response);
                    });
                });
            });
            return $q.all(promises);
        };

        /**
         * Prefetch templates, fetch detailed request objects and opens a
         * new window with a printable version of the selected request list.
         */
        $scope.printSelected = function() {
            var _printWindow = openPrintWindow();
            prefetchTemplates()
            .then(function() {
                return fetchSelected();
            })
            .then(function() {
                $scope.print_selection = _.sortBy($scope.print_selection,
                        function(r) {
                            return Number(r.requestNumber.split('-')[0]);
                        },
                        function(r) {
                            return Number(r.requestNumber.split('-')[1]);
                        }
                );
                $timeout().then(function() {
                    writeToPrintWindow(_printWindow);
                });
            });
        };

    }]);
})(window, console, angular, jQuery, _, window.bootbox);

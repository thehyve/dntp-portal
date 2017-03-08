/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('LabRequestController', [
        '$q', '$rootScope', '$scope', '$modal',
        '$templateCache', '$http',
        '$timeout',
        '$location', '$route', '$routeParams', '$window',
        '$filter',
        'Request', 'LabRequest', 'Restangular', 'LabRequestFilter',
        function (
                $q, $rootScope, $scope, $modal,
                $templateCache, $http,
                $timeout,
                $location, $route, $routeParams, $window,
                $filter,
                Request, LabRequest, Restangular, LabRequestFilter) {

            'use strict';

            $scope.labReqModal = $modal({
                id: 'labRequestWindow',
                scope: $scope,
                templateUrl: '/app/lab-request/edit-lab-request.html',
                backdrop: 'static',
                show: false
            });

            $scope.hubAssistanceModal = $modal({
                id: 'hubAssistanceWindow',
                scope: $scope,
                templateUrl: '/app/lab-request/edit-hub-assistance.html',
                backdrop: 'static',
                show: false
            });

            $scope.rejectLabRequestModal = $modal({
                id: 'rejectLabRequestWindow',
                scope: $scope,
                templateUrl: '/app/lab-request/reject-lab-request.html',
                backdrop: 'static',
                show: false
            });

            $scope.alerts = [];
            //$scope.labRequest = {};
            $scope.itemsPerPage = 20;

            var _getRecallMailRecipients = function(labRequest) {
                var recipients = [];
                recipients.push(_.get(labRequest, 'requesterEmail'));
                recipients.push(_.get(labRequest, 'request.pathologistEmail'));
                recipients = recipients.concat(_.get(labRequest, 'requesterLab.emailAddresses'));

                // remove duplicates and empty values
                recipients = _.map(_.compact(recipients), function(r) { return r.trim(); });
                recipients = _.uniq(_.compact(recipients));
                return recipients.join(', ');
            };

            $scope.getRecallMailRecipients = _getRecallMailRecipients;

            var _createSampleList = function (labRequests) {
                $scope.samples = [];
                for (var i = 0; i < labRequests.length; i++) {
                    var pathologyList = labRequests[i].pathologyList;
                    if (pathologyList !== null) {
                        for (var j in pathologyList) {
                            var item = pathologyList[j];
                            item.labRequestId = labRequests[i].id;
                            item.labRequestCode = labRequests[i].labRequestCode;
                            item.code = labRequests[i].code;
                            item.processInstanceId = labRequests[i].processInstanceId;
                            item.status = labRequests[i].status;
                            item.assignee = labRequests[i].assignee;
                            item.email = _getRecallMailRecipients(labRequests[i]);
                            $scope.samples.push(item);
                        }
                    }
                }
                //$scope.paNumbersDisplayedCollection = [].concat($scope.samples);
            };

            /* from: http://lorenzofox3.github.io/smart-table-website/ */
            $scope.getSamplesPage = function(start, number, params) {
                var deferred = $q.defer();

                var filtered = params.search.predicateObject ? $filter('filter')($scope.samples, params.search.predicateObject) : $scope.samples;

                if (params.sort.predicate) {
                    filtered = $filter('orderBy')(filtered, params.sort.predicate, params.sort.reverse);
                }

                var result = filtered.slice(start, start + number);

                $timeout(function () {
                    deferred.resolve({
                        data: result,
                        numberOfPages: Math.ceil(filtered.length / number)
                    });
                }, 1500);

                return deferred.promise;
            };

            /* from: http://lorenzofox3.github.io/smart-table-website/ */
            $scope.updateSampleTable = function(tableState) {
                var pagination = tableState.pagination;
                var start = pagination.start || 0;
                var number = pagination.number || 10;

                $scope.getSamplesPage(start, number, tableState).then(function (result) {
                    $scope.paNumbersDisplayedCollection = result.data;
                    tableState.pagination.numberOfPages = result.numberOfPages; //set the number of pages so the pagination can update
                });
            };

            //$scope.sequenceNumberColumnName = 'PALGAexcerptnr';

            $scope.getSequenceNumberForPaNumber = function (labRequest, paNumber) {
                for (var i in labRequest.excerptList.entries) {
                    var entry = labRequest.excerptList.entries[i];
                    if (entry.paNumber == paNumber) {
                        return entry.sequenceNumber;
                        //return entry.values[seqNrColumn];
                    }
                }
            };

            /**
             * To load lab request list
             * @private
             */
            var _loadRequests = function () {
                var deferred = $q.defer();
                var fetchSampleList = ($route.current.templateUrl === 'app/lab-request/samples.html');
                Restangular.all(fetchSampleList ? 'labrequests/detailed' : 'labrequests')
                .getList().then(function (labRequests) {
                    $scope.allLabRequests = labRequests;
                    $scope.allLabRequests.forEach(function(labRequest) {
                        labRequest.code = Request.convertLabRequestCode(labRequest);
                    });
                    if (fetchSampleList) {
                        _createSampleList(labRequests);
                    }
                    deferred.resolve($scope.allLabRequests);
                }, function (err) {
                    deferred.reject('Cannot load lab requests. ' + _flattenError(err));
                });
                return deferred.promise;
            };

            /**
             * Get address in html format
             * @param contactData
             * @param noEmail
             * @returns {string}
             */
            var getHTMLAddress = function (contactData, noEmail) {

                var _createEmailTmp = function (email) {
                    return '<span><i class="glyphicon glyphicon-envelope"></i></span> <a href="mailto:' +
                        email + '">' + email + '</a>';
                };

                var _createPhoneTmp = function (phone) {
                    return '<span><i class="glyphicon glyphicon-earphone"></i></span> ' + phone;
                };

                return contactData ? ''
                    .concat(contactData.address1 !== null ? contactData.address1 + '<br>' : '')
                    .concat(contactData.address2 !== null ? contactData.address2 + '<br>' : '')
                    .concat(contactData.postalCode !== null ? contactData.postalCode + '  ' : '')
                    .concat(contactData.city !== null ? contactData.city : '')
                    .concat(contactData.city !== null || contactData.postalCode !== null ? '<br>' : '')
                    .concat(contactData.country !== null ? contactData.country + '<br>' : '')
                    .concat(contactData.telephone !== null ? _createPhoneTmp(contactData.telephone) + '<br>' : '')
                    .concat(contactData.email !== null && !noEmail ? _createEmailTmp(contactData.email) + '<br>' : '')
                    : '';
            };

            /**
             * Get address in html format
             * @param contactData
             * @param noEmail
             * @returns {string}
             */
            var getHTMLAddressForLab = function (lab, noEmail) {
                var contactData = lab.contactData;
                contactData.email = lab.emailAddresses.join(',');
                return getHTMLAddress(contactData, noEmail);
            };

            /**
             * To load lab request
             * @private
             */
            var _loadRequest = function (obj) {
                var restInstance, deferred = $q.defer();

                if (obj.hasOwnProperty('get')) {
                    restInstance = obj;
                } else {
                    restInstance = Restangular.one('labrequests', obj.id);
                }

                restInstance.get().then(function (result) {
                    //result.request.type = Request.convertRequestOptsToType(result.request);
                    $scope.labRequest = result;
                    $scope.labRequest.htmlRequesterAddress = getHTMLAddress($scope.labRequest.requester.contactData);
                    $scope.labRequest.htmlRequesterLabAddress = getHTMLAddressForLab($scope.labRequest.requesterLab);
                    $scope.labRequest.htmlRequesterLabAddressPrint = getHTMLAddressForLab($scope.labRequest.requesterLab, true);
                    $scope.labRequest.htmlLabAddress = getHTMLAddressForLab($scope.labRequest.lab);
                    deferred.resolve($scope.labRequest);
                    $scope.getRequestByLabRequest($scope.labRequest);
                }, function (err) {
                    var errMsg = 'Error : ' + err.data.status + ' - ' + err.data.error;
                    $scope.alerts.push({type: 'danger', msg: errMsg});
                    deferred.reject(errMsg);
                });
                return deferred.promise;
            };

            $scope.selections = {
                overview: LabRequestFilter.selectAll,
                hub_assistance_requested: LabRequestFilter.selectHubAssistanceRequested,
                claimed: LabRequestFilter.selectClaimed($rootScope.currentUserId),
                unclaimed: LabRequestFilter.selectUnclaimed
            };
            _(LabRequest.statuses).forEach(function(status) {
                $scope.selections[status] = LabRequestFilter.selectByStatus(status);
            });

	        $scope.checkTableFilterStatus = function() {
		        // Table filter status
		        var tfs = $scope.activeSidebar;
		        $scope.tableFilterStatus = tfs;

		        if(!tfs in $scope.selections) {
			        $scope.tableFilterStatus = "";
		        }
		        // Apply the scope to trigger correct initialization of persisted local storage for smart table
		        $scope.$apply();
	        };

            $scope.showSelection = function(labRequests) {
                var selection = $scope.activeSidebar;
                if (labRequests && selection in $scope.selections) {
                    $scope.labRequests = $scope.selections[selection](labRequests);
                } else {
                    $scope.labRequests = [];
                }
                $scope.displayedLabRequests = [].concat($scope.labRequests);
            };

            $scope.$watch('allLabRequests', function(newValue) {
                if (newValue) {
                    $scope.showSelection(newValue);
	                $timeout( function() {
		                $scope.checkTableFilterStatus();
	                },10);
                }
            });

            var _loadData = function () {
                if ($routeParams.labRequestId) {
                    _loadRequest({id: $routeParams.labRequestId});
                } else {
                    if ($routeParams.selection && $routeParams.selection in $scope.selections) {
                        $scope.activeSidebar = $routeParams.selection;
                    } else if ($rootScope.isHubUser()){
                        $scope.activeSidebar = 'hub_assistance_requested';
                    } else {
                        $scope.activeSidebar = 'overview';
                    }
                    _loadRequests();
                }
            };

            _loadData();

            $scope.editHubAssistance = function (labRequest) {
                _loadRequest(labRequest).then(function (req) {
                    $scope.hubAssistanceRequest = _.clone(req);
                    $scope.hubAssistanceModal.show();
                });
            };

            $scope.cancelHubAssistance = function () {
                $scope.hubAssistanceModal.hide();
            };

            $scope.edit = function (labRequest) {
                _loadRequest(labRequest).then(function () {
                    $scope.labReqModal.show();
                });
            };

            $scope.cancel = function () {
                $scope.labReqModal.hide();
            };

            var _flattenError = function(err) {
                if (err instanceof Object) {
                    if ('message' in err) {
                        return err.message;
                    } else if ('data' in err) {
                        if ('message' in err.data) {
                            return err.data.message;
                        }
                        return JSON.stringify(err.data);
                    }
                    return JSON.stringify(err);
                } else {
                    return err;
                }
            };

            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };

            $scope.commitReject = function(labRequest, rejectReason) {
                labRequest.rejectReason = rejectReason;
                labRequest.customPUT(labRequest, 'reject').then(function () {
                    if ($scope.rejectLabRequestModal) {
                        $scope.rejectLabRequestModal.hide();
                    }
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                });
            };

            $scope.cancelReject = function() {
                if ($scope.rejectLabRequestModal) {
                    $scope.rejectLabRequestModal.hide();
                }
            };

            $scope.reject = function(labRequest) {
                $scope.labRequest = labRequest;
                $scope.rejectReason = '';
                if ($scope.rejectLabRequestModal) {
                    $scope.rejectLabRequestModal.show();
                }
            };

            $scope.undoReject = function (labRequest) {
                bootbox.confirm($rootScope.translate('Return this lab request to status \'Under review by lab\'?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'undoreject').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.approve = function (labRequest) {
                bootbox.confirm($rootScope.translate('Do you want to approve this request to your laboratory?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'approve').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            // Check if hub assistance is enabled.
                            // Otherwise just reload the data.
                            if(labRequest.lab.hubAssistanceEnabled) {
                                $scope.editHubAssistance(labRequest);
                            } else {
                                _loadData();
                            }
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.sending = function (labRequest) {
                bootbox.confirm($rootScope.translate('Have you sent the material to the requester?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT({}, 'sending').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.received = function (labRequest) {
                bootbox.confirm($rootScope.translate('Did you receive the material?'),
                        function (result) {
                    if (result) {
                        var obj = { id: labRequest.id,
                            samplesMissing: labRequest.samplesMissing,
                            missingSamples: labRequest.missingSamples};

                        labRequest.customPUT(obj, 'received').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.returning = function (labRequest) {
                bootbox.confirm($rootScope.translate('Do you want to return the material?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT({}, 'returning').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.completeReturned = function (labRequest) {
                bootbox.confirm($rootScope.translate('Did you receive the material in return?'),
                        function (result) {
                    if (result) {
                        var obj = { id: labRequest.id,
                            samplesMissing: labRequest.samplesMissing,
                            missingSamples: labRequest.missingSamples};
                        labRequest.customPUT(obj, 'completereturned').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.completeReportsOnly = function (labRequest) {
                bootbox.confirm($rootScope.translate('Do you want to finish the request for PA reports and/or clinical data?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'completereportsonly').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.completeRejected = function (labRequest) {
                bootbox.confirm($rootScope.translate('Do you want to complete the rejected request?'),
                        function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'completerejected').then(function () {
                            if ($scope.labReqModal) {
                                $scope.labReqModal.hide();
                            }
                            _loadData();
                        }, function (err) {
                            $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                        });
                    }
                });
            };

            $scope.claim = function (labRequest) {
                labRequest.customPUT({}, 'claim')
                    .then(function () {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.unclaim = function (labRequest) {
                labRequest.customPUT({}, 'unclaim')
                    .then(function () {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.statuses = LabRequest.statuses;

            $scope.lab_user_statuses = _.difference($scope.statuses, [
                'Returned',
                'Completed'
            ]);

            $scope.hub_user_statuses = _.difference($scope.statuses, [
                'Waiting for lab approval',
                'Returned',
                'Completed'
            ]);

            $scope.isLabUserStatus = function (status) {
                return _.includes($scope.lab_user_statuses, status);
            };

            $scope.isHubUserStatus = function (status) {
                return _.includes($scope.hub_user_statuses, status);
            };

            $scope.isLabOrHubUserStatus = function (status) {
                return  ($rootScope.isLabUser() && $scope.isLabUserStatus(status)) ||
                        ($rootScope.isHubUser() && $scope.isHubUserStatus(status));
            };

            $scope.isPalgaStatus = function (status) {
                return $rootScope.isPalga() && status == 'Rejected';
            }

            $scope.requester_statuses = [
                'Sending',
                'Received'
            ];

            $scope.isRequesterStatus = function (status) {
                return $rootScope.isRequester() && _.includes($scope.requester_statuses, status);
            };

            $scope.update = function (labRequest) {
                var obj = {
                    'paReportsSent': labRequest.paReportsSent,
                    'clinicalDataSent': labRequest.clinicalDataSent,
                    'hubAssistanceRequested': labRequest.hubAssistanceRequested
                };
                Restangular.one('labrequests', labRequest.id)
                    .customPUT(obj).then(function () {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    if ($scope.hubAssistanceModal) {
                        $scope.hubAssistanceModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                });
            };

            $scope.updatePathology = function (labRequest, pathology) {
                var obj = {};
                obj.samples = [];
                for (var i in pathology.samples) {
                    obj.samples.push(pathology.samples[i].text);
                }
                if (obj.samples.length > 0) {
                    obj.samplesAvailable = true;
                } else {
                    obj.samplesAvailable = pathology.samplesAvailable;
                }
                Restangular.one('labrequests', labRequest.id).one('pathology', pathology.id)
                    .customPUT(obj).then(function () {

                    },
                    function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.editPathology = {};

            $scope.addPathology = function (labRequest, pathology) {
                Restangular.one('labrequests', labRequest.id).post('pathology', pathology)
                    .then(function () {
                        $scope.editPathology = {};
                        _loadData();
                    },
                    function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.deletePathology = function (labRequest, pathology) {
                bootbox.confirm(
                    '<h4>' +
                    $rootScope.translate('Are you sure you want to delete the PA number?') +
                    '</h4>',
                    function(result) {
                        if (result) {
                            Restangular.one('labrequests', labRequest.id).one('pathology', pathology.id)
                            .remove().then(function () {
                                _loadData();
                            },
                            function (err) {
                                $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                            });
                        }
                    }
                );
            };

            $scope.printPreview = function () {

                var _contents = '<html><head><link rel="stylesheet" type="text/css" href="./css/print.css" />' +
                    '</head><body onload="window.print()">'
                        .concat('<div class="header-div"><h1>'+ $rootScope.translate('PALGA request') + ':' + document.getElementById('lab-request-title').innerHTML + '</h1>')
                        .concat('<img src="images/logo_palga-transparent.png" alt="PALGA" width="144" height="75"></div>')
                        .concat(document.getElementById('pa-numbers').innerHTML)
                        .concat('</body></html>');
                var _printWindow = window.open('', '_blank');
                _printWindow.document.write(_contents);
                _printWindow.document.close();
            };

            $scope.toggleAvailability = function (labRequest, pathology) {
                if (pathology.samplesAvailable === null) {
                    pathology.samplesAvailable = true;
                }
                pathology.samplesAvailable = !pathology.samplesAvailable;
                $scope.updatePathology(labRequest,  pathology)
            };

            $scope.selected_lab_requests = {};
            $scope.filteredCollection = [];

            $scope.toggleSelect = function(filteredCollection) {
                if (_.includes($scope.selected_lab_requests, true)) {
                    $scope.selected_lab_requests = {};
                } else {
                    $scope.selected_lab_requests = _.fromPairs(
                            _.map(filteredCollection, function(labRequest) {
                                return [labRequest.id, true];
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
                 'app/lab-request/lab-request-contents.html',
                 'app/lab-request/comments.html'
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
                var selected = _.transform($scope.selected_lab_requests, function(result, value, key) {
                    if (value) { result.push(key); }
                  }, []);
                $scope.print_selection = [];
                var promises = _.map(selected, function(labRequestId) {
                    return $q(function(resolve, reject) {
                        _loadRequest({id: labRequestId}).then(function(req) {
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
                                return Number(r.labRequestCode.split('-')[0]);
                            },
                            function(r) {
                                return Number(r.labRequestCode.split('-')[1]);
                            },
                            function(r) {
                                return Number(r.labRequestCode.split('-')[2]);
                            }
                    );
                    $timeout().then(function() {
                        writeToPrintWindow(_printWindow);
                    });
                });
            };

            $scope.getRequestByLabRequest = function(labRequest) {
                Request.get({id:labRequest.processInstanceId}, function (req) {
                    var now = new Date();
                    req.date = now.getDate() + '-' + now.getMonth() + '-' + now.getFullYear();
                    $scope.request = req;
                    $rootScope.tempRequest = jQuery.extend( true, {}, req ); // deep copy
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };
        }

        ]);

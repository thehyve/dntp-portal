/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('LabRequestController', [
        '$q', '$rootScope', '$scope', '$modal',
        '$location', '$route', '$routeParams', '$window',
        'Request', 'LabRequest', 'Restangular', 'LabRequestFilter',
        function (
                $q, $rootScope, $scope, $modal,
                $location, $route, $routeParams, $window,
                Request, LabRequest, Restangular, LabRequestFilter) {
            'use strict';

            $scope.labReqModal = $modal({
                id: 'labRequestWindow',
                scope: $scope,
                templateUrl: '/app/lab-request/edit-lab-request.html',
                backdrop: 'static',
                show: false
            });

            $scope.alerts = [];
            $scope.labRequest = {};
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
            }
            $scope.getRecallMailRecipients = _getRecallMailRecipients;

            var _createSampleList = function (labRequests) {
                //console.log('_createSampleList: ' + labRequests.length + ' lab requests.');
                $scope.samples = [];
                for (var i = 0; i < labRequests.length; i++) {
                    var pathologyList = labRequests[i].pathologyList;
                    if (pathologyList !== null) {
                        for (var j in pathologyList) {
                            var item = pathologyList[j];
                            item.labRequestId = labRequests[i].id;
                            item.labRequestCode = labRequests[i].labRequestCode;
                            item.processInstanceId = labRequests[i].processInstanceId;
                            item.status = labRequests[i].status;
                            item.assignee = labRequests[i].assignee;
                            item.email = _getRecallMailRecipients(labRequests[i]);
                            $scope.samples.push(item);
                        }
                    }
                }
                //console.log('_createSampleList: ' + $scope.samples.length + ' samples.');
                $scope.paNumbersDisplayedCollection = [].concat($scope.samples);
            };

            //$scope.sequenceNumberColumnName = 'PALGAexcerptnr';

            $scope.getSequenceNumberForPaNumber = function (labRequest, paNumber) {
                /*var seqNrColumn = labRequest.excerptList.columnNames.indexOf($scope.sequenceNumberColumnName);
                if (seqNrColumn < 0) {
                    console.log('Error: column with name ' + $scope.sequenceNumberColumnName + ' not found in excerpt list.');
                    return '';
                }*/
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
             * @returns {string}
             */
            var getHTMLAddress = function (contactData) {

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
                    .concat(contactData.email !== null ? _createEmailTmp(contactData.email) + '<br>' : '')
                    : '';
            };

            /**
             * Get address in html format
             * @param contactData
             * @returns {string}
             */
            var getHTMLAddressForLab = function (lab) {
                var contactData = lab.contactData;
                contactData.email = lab.emailAddresses.join(', ');
                return getHTMLAddress(contactData);
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
                    result.request.type = Request.convertRequestOptsToType(result.request);
                    $scope.labRequest = result;
                    $scope.labRequest.htmlRequesterAddress = getHTMLAddress($scope.labRequest.requester.contactData);
                    $scope.labRequest.htmlRequesterLabAddress = getHTMLAddressForLab($scope.labRequest.requesterLab);
                    $scope.labRequest.htmlLabAddress = getHTMLAddressForLab($scope.labRequest.lab);
                    deferred.resolve($scope.labRequest);
                }, function (err) {
                    var errMsg = 'Error : ' + err.data.status + ' - ' + err.data.error;
                    $scope.alerts.push({type: 'danger', msg: errMsg});
                    deferred.reject(errMsg);
                });
                return deferred.promise;
            };

            $scope.selections = {
                overview: LabRequestFilter.selectAll,
                claimed: LabRequestFilter.selectClaimed($rootScope.currentUserId),
                unclaimed: LabRequestFilter.selectUnclaimed
            };
            _(LabRequest.statuses).forEach(function(status) {
                $scope.selections[status] = LabRequestFilter.selectByStatus(status);
            });

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
                }
            });

            var _loadData = function () {
                if ($routeParams.labRequestId) {
                    _loadRequest({id: $routeParams.labRequestId});
                } else {
                    if ($routeParams.selection && $routeParams.selection in $scope.selections) {
                        $scope.activeSidebar = $routeParams.selection;
                    } else {
                        $scope.activeSidebar = 'overview';
                    }
                    _loadRequests();
                }
            };

            _loadData();

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

            $scope.reject = function (labRequest) {
                bootbox.confirm(
                    '<h4>Are you sure you want to reject the lab request?</h4>\n' +
                    '<form id="reject" action="">' +
                    $rootScope.translate('Please enter the reason for rejection.') +
                    '\n<br><br>\n' +
                    '<textarea type="text" class="form-control" name="rejectReason" id="rejectReason" required autofocus ng-model="rejectReason"></textarea>' +
                    '</form>',
                    function(result) {
                        if (result) {
                            labRequest.rejectReason = jQuery('#rejectReason').val();
                            //console.log('Rejected. Reason: ' + labRequest.rejectReason);
                            labRequest.customPUT(labRequest, 'reject').then(function () {
                                if ($scope.labReqModal) {
                                    $scope.labReqModal.hide();
                                }
                                _loadData();
                            }, function (err) {
                                $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                            });
                        }
                    }
                );
            };

            $scope.accept = function (labRequest) {
                bootbox.confirm('Accept this lab request?' , function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'accept').then(function () {
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


            $scope.sending = function (labRequest) {
                bootbox.confirm('Send this lab request?'  , function (result) {
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
                bootbox.confirm('Is the lab request received?'  , function (result) {
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
                bootbox.confirm('Return this lab request?'  , function (result) {
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

            $scope.returned = function (labRequest) {
                bootbox.confirm('Is the lab request returned?'  , function (result) {
                    if (result) {
                        var obj = { id: labRequest.id,
                            samplesMissing: labRequest.samplesMissing,
                            missingSamples: labRequest.missingSamples};
                        labRequest.customPUT(obj, 'returned').then(function () {
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

            $scope.complete = function (labRequest) {
                bootbox.confirm('Complete lab request?'  , function (result) {
                    if (result) {
                        labRequest.customPUT(labRequest, 'complete').then(function () {
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
                'Rejected',
                'Completed'
            ]);

            $scope.hub_user_statuses = _.difference($scope.statuses, [
                'Waiting for lab approval',
                'Returned',
                'Rejected',
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

            $scope.requester_statuses = [
                'Sending',
                'Received'
            ];

            $scope.isRequesterStatus = function (status) {
                return _.includes($scope.requester_statuses, status);
            };

            $scope.update = function (labRequest) {
                var obj = {
                    'paReportsSent': labRequest.paReportsSent,
                    'hubAssistanceRequested': labRequest.hubAssistanceRequested
                };
                Restangular.one('labrequests', labRequest.id)
                    .customPUT(obj).then(function () {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                });
            };

            $scope.updatePathology = function (labRequest, pathology) {
                //labrequests/{id}/pathology/{pathologyId}
                var obj = {};
                obj.samples = [];
                for (var i in pathology.samples) {
                    obj.samples.push(pathology.samples[i].text);
                }
                Restangular.one('labrequests', labRequest.id).one('pathology', pathology.id)
                    .customPUT(obj).then(function () {
                        //console.log(result);
                    },
                    function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.editPathology = {};
            
            $scope.addPathology = function (labRequest, pathology) {
                Restangular.one('labrequests', labRequest.id).post('pathology', pathology)
                    .then(function () {
                        //console.log(result);
                        $scope.editPathology = {};
                        _loadData();
                    },
                    function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };
            
            $scope.deletePathology = function (labRequest, pathology) {
                bootbox.confirm(
                    '<h4>Are you sure you want to delete the PA number?</h4>',
                    function(result) {
                        if (result) {
                            Restangular.one('labrequests', labRequest.id).one('pathology', pathology.id)
                            .remove().then(function () {
                                //console.log(result);
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
                        .concat('<h1>' + document.getElementById('lab-request-title').innerHTML + '</h1>')
                        .concat(document.getElementById('lab-request-details').innerHTML)
                        .concat('</body></html>');
                var _printWindow = window.open('', '_blank');
                _printWindow.document.write(_contents);
                _printWindow.document.close();
            };

        }]);

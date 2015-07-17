'use strict';

angular.module('ProcessApp.controllers')
    .controller('LabRequestController', [
        '$q', '$rootScope', '$scope', '$modal', '$location', '$route', '$routeParams', '$window', 'Request',
        'Restangular', function ($q, $rootScope, $scope, $modal, $location, $route, $routeParams, $window, Request,
                                 Restangular) {

            $rootScope.redirectUrl = $location.path();

            $scope.login = function () {
                $location.path('/login');
            };

            if (!$rootScope.globals.currentUser) {
                $scope.login();
            }

            $scope.labReqModal = $modal({
                id: 'labRequestWindow',
                scope: $scope,
                template: '/app/lab-request/edit-lab-request.html',
                backdrop: 'static',
                show: false
            });

            $scope.alerts = [];
            $scope.labRequest = {};

            $scope.getName = function (user) {
                if (user === null) {
                    return '';
                }
                return user.firstName +
                    ((user.firstName === '' || user.lastName === '' || user.lastName === null ) ? '' : ' ') +
                    (user.lastName === null ? '' : user.lastName);
            };

            var _createSampleList = function (labRequests) {
                $scope.samples = [];
                for (var i = 0; i < labRequests.length; i++) {
                    var pathologyList = labRequests[i].pathologyList;
                    if (pathologyList != null) {
                        for (var j in pathologyList) {
                            var item = pathologyList[j];
                            item.labRequestId = labRequests[i].id;
                            item.labRequestCode = labRequests[i].labRequestCode;
                            item.processInstanceId = labRequests[i].processInstanceId;
                            item.status = labRequests[i].status;
                            item.email = labRequests[i].requesterLab.contactData.email
                                ? labRequests[i].requesterLab.contactData.email
                                : labRequests[i].requesterEmail;
                            $scope.samples.push(item);
                        }
                    }
                }
                $scope.paNumbersDisplayedCollection = [].concat($scope.samples);
            };

            /**
             * To load lab request list
             * @private
             */
            var _loadRequests = function () {
                var deferred = $q.defer();
                Restangular.all('labrequests').getList().then(function (labRequests) {
                    $rootScope.labRequests = labRequests;
                    $scope.displayedLabRequests = [].concat($rootScope.labRequests);
                    if ($route.current.templateUrl === 'app/lab-request/samples.html') {
                        _createSampleList(labRequests);
                    }
                    deferred.resolve($scope.labRequests);
                }, function (err) {
                    if (err.status === 403) {
                        $rootScope.errormessage = err.data.message;
                        $scope.login();
                        return;
                    }
                    deferred.reject('Cannot load lab requests. ' + _flattenError(err));
                });
                return deferred.promise;
            };

            /**
             * Get address in html format
             * @param contactData
             * @returns {string}
             */
            var getHTMLRequesterAddress = function (contactData) {

                var _createEmailTmp = function (email) {
                    return '<span><i class="glyphicon glyphicon-envelope"></i></span> <a href="mailto:' + email
                        + '">' + email + '</a>';
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
                    .concat(contactData.stateProvince !== null ? contactData.stateProvince + ', ' : '')
                    .concat(contactData.stateProvince !== null ? contactData.country + '<br>' : '')
                    .concat(contactData.telephone !== null ? _createPhoneTmp(contactData.telephone) + '<br>' : '')
                    .concat(contactData.email !== null ? _createEmailTmp(contactData.email) + '<br>' : '')
                    : '';
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
                    $scope.labRequest.htmlRequesterAddress = getHTMLRequesterAddress($scope.labRequest.requester.contactData);
                    $scope.labRequest.htmlRequesterLabAddress = getHTMLRequesterAddress($scope.labRequest.requesterLab.contactData);
                    $scope.labRequest.htmlLabAddress = getHTMLRequesterAddress($scope.labRequest.lab.contactData);
                    deferred.resolve($scope.labRequest);
                }, function (err) {
                    if (err.status === 403) {
                        $rootScope.errormessage = err.data.message;
                        $scope.login();
                        return;
                    }
                    var errMsg = 'Error : ' + err.data.status + ' - ' + err.data.error;
                    $scope.alerts.push({type: 'danger', msg: errMsg});
                    deferred.reject(errMsg);
                });
                return deferred.promise;
            };

            var _loadData = function () {
                if ($routeParams.labRequestId) {
                    _loadRequest({id: $routeParams.labRequestId});
                } else {
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
            }
            
            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };

            $scope.reject = function (labRequest) {
                bootbox.confirm(
                    '<h4>Are you sure you want to reject the lab request?</h4>\n' +
                    '<form id="reject" action="">' +
                    'Please enter a reject reason:\n<br><br>\n' +
                    '<textarea type="text" class="form-control" name="rejectReason" id="rejectReason" required autofocus ng-model="rejectReason"></textarea>' +
                    '</form>',
                    function(result) {
                        if (result) {
                            labRequest.rejectReason = $('#rejectReason').val();
                            //console.log('Rejected. Reason: ' + labRequest.rejectReason);
                            labRequest.customPUT(labRequest, 'reject').then(function (result) {
                                if ($scope.labReqModal) {
                                    $scope.labReqModal.hide();
                                }
                                _loadData();
                            }
                            , function (err) {
                                $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                            });
                        }
                    }
                );
            };

            $scope.accept = function (labRequest) {
                bootbox.confirm('Accept this lab request?' , function (result) {
                    if (result) {
                        labRequest.customPUT({}, 'accept').then(function (result) {
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
                        labRequest.customPUT({}, 'sending').then(function (result) {
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

                        labRequest.customPUT(obj, 'received').then(function (result) {
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
                        labRequest.customPUT({}, 'returning').then(function (result) {
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
                        labRequest.customPUT(obj, 'returned').then(function (result) {
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
                        labRequest.customPUT(labRequest, 'complete').then(function (result) {
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
                    .then(function (result) {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.unclaim = function (labRequest) {
                labRequest.customPUT({}, 'unclaim')
                    .then(function (result) {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.lab_user_statuses = [
                'Waiting for lab approval',
                'Approved',
                'Sending',
                'Received',
                'Returning'
            ];

            $scope.isLabUserStatus = function (status) {
                return $scope.lab_user_statuses.indexOf(status) !== -1;
            };

            $scope.requester_statuses = [
                'Sending',
                'Received'
            ];

            $scope.isRequesterStatus = function (status) {
                return $scope.requester_statuses.indexOf(status) !== -1;
            };

            $scope.isLabUser = function () {
                if (!$rootScope.globals.currentUser) {
                    $scope.login();
                    return;
                }
                return $rootScope.globals.currentUser.roles.indexOf('lab_user') !== -1;
            };

            $scope.isRequester = function () {
                if (!$rootScope.globals.currentUser) {
                    $scope.login();
                    return;
                }
                return $rootScope.globals.currentUser.roles.indexOf('requester') !== -1;
            };

            $scope.update = function (labRequest) {
                var obj = {'paReportsSent': labRequest.paReportsSent};
                Restangular.one('labrequests', labRequest.id)
                    .customPUT(obj).then(function (result) {
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
                    .customPUT(obj).then(function (result) {
                        //console.log(result);
                    },
                    function (err) {
                        $scope.alerts.push({type: 'danger', msg: _flattenError(err)});
                    });
            };

            $scope.editPathology = {};
            
            $scope.addPathology = function (labRequest, pathology) {
                Restangular.one('labrequests', labRequest.id).post('pathology', pathology)
                    .then(function (result) {
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
                            .remove().then(function (result) {
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
            
            $scope.focus = function (el) {
                $(el).focus();
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
            }

        }]);

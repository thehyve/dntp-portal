'use strict';

angular.module('ProcessApp.controllers')
    .controller('LabRequestController', [
        '$q', '$rootScope', '$scope', '$modal', '$location', '$route', '$routeParams', '$window', 'Request',
        'Restangular', function ($q, $rootScope, $scope, $modal, $location, $route, $routeParams, $window, Request,
                                 Restangular) {

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
                    deferred.reject('Cannot load lab requests. ' + err);
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
                    .concat(contactData.city !== null ? contactData.city + ' ' : '')
                    .concat(contactData.postalCode !== null ? contactData.postalCode + '<br>' : '')
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

            $scope.closeAlert = function (index) {
                $scope.alerts.splice(index, 1);
            };

            $scope.reject = function (labRequest) {
                bootbox.prompt({
                    title: 'Are you sure you want to reject the lab request?\n<br>' +
                    'Please enter a reject reason:',
                    callback: function (result) {
                        if (result) {
                            labRequest.rejectReason = result;
                            labRequest.customPUT(labRequest, 'reject').then(function (result) {
                                    if ($scope.labReqModal) {
                                        $scope.labReqModal.hide();
                                    }
                                    $location.path('/lab-request/view/' + labRequest.id);
                                    //_loadRequests();
                                }
                                , function (err) {
                                    console.error('Error: ', err);
                                    $scope.alerts.push({type: 'danger', msg: err});
                                });
                        }
                    }
                });
            };

            $scope.accept = function (labRequest) {
                labRequest.customPUT({}, 'accept').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };


            $scope.sending = function (labRequest) {
                labRequest.customPUT({}, 'sending').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };

            $scope.received = function (labRequest) {
                labRequest.customPUT(labRequest, 'received').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };

            $scope.returning = function (labRequest) {
                labRequest.customPUT({}, 'returning').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };

            $scope.returned = function (labRequest) {
                labRequest.customPUT(labRequest, 'returned').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };

            $scope.complete = function (labRequest) {
                labRequest.customPUT(labRequest, 'complete').then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
                });
            };

            $scope.claim = function (labRequest) {
                labRequest.customPUT({}, 'claim')
                    .then(function (result) {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: err});
                    });
            };

            $scope.unclaim = function (labRequest) {
                labRequest.customPUT({}, 'unclaim')
                    .then(function (result) {
                        _loadData();
                    }, function (err) {
                        $scope.alerts.push({type: 'danger', msg: err});
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
                labRequest.put().then(function (result) {
                    if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                    }
                    _loadData();
                }, function (err) {
                    $scope.alerts.push({type: 'danger', msg: err});
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
                        $scope.alerts.push({type: 'danger', msg: err});
                    });
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

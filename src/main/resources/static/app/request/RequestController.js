'use strict';

angular.module('ProcessApp.controllers')
    .controller('RequestController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'RequestAttachment', 'RequestComment',
        'ApprovalComment', 'ApprovalVote',
        'FlowOptionService', '$routeParams',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, RequestAttachment, RequestComment,
                  ApprovalComment, ApprovalVote,
                  FlowOptionService, $routeParams) {

            $rootScope.redirectUrl = $location.path();
        
            $scope.login = function() {
                $location.path('/login');
            };

            if (!$rootScope.globals.currentUser) {
                $scope.login();
                return;
            }
        
            $scope.serverurl = $location.protocol()+'://'+$location.host()
                +(($location.port()===80) ? '' : ':'+$location.port());

            $scope.error = '';
            $rootScope.tempRequest = null;

            if ($routeParams.requestId) {
                if (!$scope.requests) {
                    $scope.requests = [];
                }
                $scope.editComment = {};
                $scope.approvalComment = {};
                $scope.commentEditVisibility = {};
                Request.get({id:$routeParams.requestId}, function (req) {
                    req.type = Request.convertRequestOptsToType(req);
                    $scope.request = req;
                    $rootScope.tempRequest = $.extend( true, {}, req ); // deep copy
                }, function(response) {
                    if (response.data) {
                        $scope.error = response.data.message + '\n';
                        if (response.data.error === 302) {
                            $scope.accessDenied = true;
                        }
                        else if (response.status === 403) {
                            $rootScope.errormessage = response.data.message;
                            $scope.login();
                            return;
                        }
                    } else {
                        $scope.login();
                        return;
                    }
                });
            } else {
                Request.query().$promise.then(function(response) {
                    $scope.requests = response ? response : [];
                    $scope.displayedCollection = [].concat($scope.requests);
                }, function(response) {
                    if (response.data) {
                        $scope.error = response.data.message + '\n';
                        if (response.data.error === 302) {
                            $scope.accessDenied = true;
                            console.log("ACCESS DENIED");
                        }
                        else if (response.status === 403) {
                            //$rootScope.errormessage = response.data.message;
                            $scope.login();
                            return;
                        }
                    } else {
                        $scope.login();
                        return;
                    }
                });
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
            
            $scope.flow_options = function(options) {
                return FlowOptionService.get_default(options);
            };

            $scope.fileuploadsuccess = function(request, data, excerpts) {
                if (excerpts) {
                    $scope.excerptlist_upload_result = "success";
                } else {
                    $scope.fileupload_result = "success";
                }
                var result = new Request(JSON.parse(data));
                //$scope.refresh(request, result);
                request.attachments = result.attachments;
                request.agreementAttachments = result.agreementAttachments;
                request.excerptList = result.excerptList;
                request.dataAttachments = result.dataAttachments;
                request.medicalEthicalCommitteeApprovalAttachments = result.medicalEthicalCommitteeApprovalAttachments;
            };

            $scope.fileuploaderror = function(data, excerpts) {
                if (excerpts) {
                    $scope.excerptlist_upload_error = data;
                    $scope.excerptlist_upload_result = "error";
                } else {
                    $scope.fileupload_result = "error";
                }
            };

            $scope.start = function() {
                $scope.dataLoading = true;
                new Request().$save(function(request) {
                    //$scope.requests.unshift(request);
                    $scope.edit(request);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

            $scope.filterEmptyRequests = function(value, index) {
                return !value;
            };
            
            $scope.refresh = function(request, result) {
                result.type = Request.convertRequestOptsToType(result);
                var index = -1;
                for (var i in $scope.requests) {
                    if ($scope.requests[i].processInstanceId === request.processInstanceId) {
                        index = i;
                        break;
                    }
                }
                $scope.requests[index] = result;
                $route.reload();
                
                /* Ugly hack to prevent having to reload the controller. 
                 * Problem: smart table is only updated after insert or delete,
                 * not after updating a field. 
                 * Hack: insert an empty element and filter empty elements out.
                 */
                //$scope.requests.push({});
                
                $scope.request = result;
            };

            $scope.update = function(request) {
                $scope.dataLoading = true;
                Request.convertRequestTypeToOpts(request); // convert request type
                request.$update(function(result) {
                    $scope.refresh(request, result);
                    $scope.editRequestModal.hide();
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

            $scope.removeAgreementFile = function(f) {
                bootbox.confirm('Are you sure you want to delete file ' +
                f.name +
                '?', function(result) {
                    if (result) {
                        var attachment = new RequestAttachment();
                        attachment.requestId = $scope.request.processInstanceId;
                        attachment.id = f.id;
                        attachment.$removeAgreementFile(function(result) {
                            $scope.request.agreementAttachments.splice($scope.request.agreementAttachments.indexOf(f), 1);
                            bootbox.alert('File ' + f.name + ' deleted.');
                        }, function(response) {
                            $scope.error = response.statusText;
                        });
                    }
                });
            };

            $scope.removeMECFile = function(f) {
                bootbox.confirm('Are you sure you want to delete file ' +
                f.name +
                '?', function(result) {
                    if (result) {
                        var attachment = new RequestAttachment();
                        attachment.requestId = $scope.request.processInstanceId;
                        attachment.id = f.id;
                        attachment.$removeMECFile(function(result) {
                            $scope.request.medicalEthicalCommitteeApprovalAttachments.splice($scope.request.medicalEthicalCommitteeApprovalAttachments.indexOf(f), 1);
                            bootbox.alert('File ' + f.name + ' deleted.');
                        }, function(response) {
                            $scope.error = response.statusText;
                        });
                    }
                });
            };
            
            $scope.removeDataFile = function(f) {
                bootbox.confirm('Are you sure you want to delete file ' +
                f.name +
                '?', function(result) {
                    if (result) {
                        var attachment = new RequestAttachment();
                        attachment.requestId = $scope.request.processInstanceId;
                        attachment.id = f.id;
                        attachment.$removeDataFile(function(result) {
                            $scope.request.dataAttachments.splice($scope.request.agreementAttachments.indexOf(f), 1);
                            bootbox.alert('File ' + f.name + ' deleted.');
                        }, function(response) {
                            $scope.error = response.statusText;
                        });
                    }
                });
            };

            $scope.removeFile = function(f) {
                bootbox.confirm('Are you sure you want to delete file ' +
                f.name +
                '?', function(result) {
                    if (result) {
                        var attachment = new RequestAttachment();
                        attachment.requestId = $scope.request.processInstanceId;
                        attachment.id = f.id;
                        attachment.$remove(function(result) {
                            $scope.request.attachments.splice($scope.request.attachments.indexOf(f), 1);
                            bootbox.alert('File ' + f.name + ' deleted.');
                        }, function(response) {
                            $scope.error = response.statusText;
                        });
                    }
                });
            };

            $scope.remove = function(request) {
                bootbox.confirm('Are you sure you want to delete request ' +
                request.processInstanceId +
                '?', function(result) {
                    if (result) {
                        request.$remove(function(result) {
                            $scope.requests.splice($scope.requests.indexOf(request), 1);
                            bootbox.alert('Request ' + request.processInstanceId + ' deleted.');
                        }, function(response) {
                            $scope.error = response.statusText;
                        });
                    }
                });
            };

            $scope.submitRequest = function(request) {
                $scope.dataLoading = true;
                bootbox.confirm(
                    'Are you sure you want to submit the request?\n ' +
                    'After submission the request cannot be edited anymore.',
                    function(confirmed) {
                        if (confirmed) {
                            Request.convertRequestTypeToOpts(request); // convert request type
                            request.$submit(function(result) {
                                $scope.refresh(request, result);
                                $scope.editRequestModal.hide();
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
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
                    'Are you sure you want to submit the request for approval?',
                    function(confirmed) {
                        if (confirmed) {
                            request.$submitForApproval(function(result) {
                                $scope.refresh(request, result);
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
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
                    'Are you sure you want to finalise the request?',
                    function(confirmed) {
                        if (confirmed) {
                            request.$finalise(function(result) {
                                $scope.refresh(request, result);
                                $scope.editRequestModal.hide();
                                $scope.dataLoading = false;
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
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
                    'Are you sure you want to close the request?\n<br>' +
                    'After closing, no data files can be added.',
                    function(confirmed) {
                        if (confirmed) {
                            request.$close(function(result) {
                                $scope.refresh(request, result);
                                //$scope.editRequestModal.hide();
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
                            });
                        } else {
                            $scope.dataLoading = false;
                            $scope.$apply();
                        }
                    });
            };

            $scope.reject = function(request) {
                $scope.dataLoading = true;
                bootbox.confirm(
                    '<h4>Are you sure you want to reject the request?</h4>\n' +
                    '<form id="reject" action="">' +
                    'Please enter a reject reason:\n<br><br>\n' +
                    '<textarea type="text" class="form-control" name="rejectReason" id="rejectReason" required autofocus ng-model="rejectReason"></textarea>' +
                    '</form>',
                    function(result) {
                        if (result) {
                            request.rejectReason = $('#rejectReason').val();
                            request.$reject(function(result) {
                                $scope.refresh(request, result);
                                $scope.editRequestModal.hide();
                                $scope.dataLoading = false;
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
                                $scope.dataLoading = false;
                            });
                        }
                    }
                );
            };

            $scope.approveSelection = function(request) {
                bootbox.confirm(
                    'Are you sure you want to approve the selection?\n<br>' +
                    'After approving, lab requests will be generated.',
                    function(confirmed) {
                        if (confirmed) {
                            request.selectionApproved = true;
                            request.$updateExcerptSelectionApproval(function(result) {
                                $scope.refresh(request, result);
                                $scope.dataLoading = false;
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
                                $scope.dataLoading = false;
                            });
                        }
                    });
            };
            
            $scope.rejectSelection = function(request) {
                bootbox.confirm(
                    'Are you sure you want to reject the selection?\n<br>' +
                    'After rejecting, the status will return to \'Data delivery.\'',
                    function(confirmed) {
                        if (confirmed) {
                            request.selectionApproved = false;
                            request.$updateExcerptSelectionApproval(function(result) {
                                $scope.refresh(request, result);
                                $scope.dataLoading = false;
                            }, function(response) {
                                $scope.error = $scope.error + response.data.message + '\n';
                                $scope.dataLoading = false;
                            });
                        }
                    });
            };
            
            $scope.uploadDataFile = function(flow) {
                var max_size = 1024*1024*10;
                if (flow.getSize() > max_size) {
                    var mb_size = (flow.getSize()/(1024*1024)).toFixed(1);
                    bootbox.alert('File too large (' + mb_size + ' MB). Maximum size is 10 MB.');
                    flow.cancel();
                } else {
                    flow.upload();
                }
            };

            $scope.view = function(request) {
                $location.path('/request/view/' + request.processInstanceId);
            };

            $scope.cancel = function (request) {
                if ($rootScope.tempRequest.title === null) {
                    request.$remove(function (result) {
                        $scope.requests.splice($scope.requests.indexOf(request), 1);
                        $scope.refresh(request, result);
                    }, function (response) {
                        $scope.error = response.statusText;
                    });
                } else {
                    // return request to the original
                    $scope.request = $.extend( true, {},  $rootScope.tempRequest ); // deep copy
                }
                $scope.editRequestModal.hide();
            };

            $scope.edit = function(request) {
                if (request) {
                    Request.get({id:request.processInstanceId}, function (data) {
                        data.type = Request.convertRequestOptsToType(data);
                        $scope.request = data;
                        $rootScope.tempRequest = $.extend( true, {}, data ); // deep copy

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
                        $scope.editComment = {};
                        $scope.approvalComment = {};
                        $scope.commentEditVisibility = {};
                        if (data.returnDate === null) {
                            data.returnDate = new Date();
                        }
                        $scope.editRequestModal = $modal({
                            id: 'editRequestWindow',
                            scope: $scope,
                            template: '/app/request/edit-request.html',
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
                    result.type = Request.convertRequestOptsToType(result);
                    $scope.requests[$scope.requests.indexOf(request)] = result;
                }, function(response) {
                    $scope.error = response.statusText;
                });
            };

            $scope.unclaim = function(request) {
                request.$unclaim(function(result) {
                    result.type = Request.convertRequestOptsToType(result);
                    $scope.requests[$scope.requests.indexOf(request)] = result;
                }, function(response) {
                    $scope.error = response.statusText;
                });
            };

            $scope.focus = function (el) {
                $(el).focus();
            };

            $scope.getName = function(user) {
                if (user === null) {
                    return '';
                }
                return user.firstName +
                    ((user.firstName ==='' || user.lastName ==='' || user.lastName === null ) ? '' : ' ') +
                    (user.lastName === null ? '' : user.lastName);
            };
            $rootScope.getName = $scope.getName;

            $scope.size = function(obj) {
                var size = 0, key;
                for (key in obj) {
                    if (obj.hasOwnProperty(key)) {size++};
                }
                return size;
            };
            
            $scope.isPalga = function() {
                return $scope.globals.currentUser.roles.indexOf('palga') != -1;
            };

            $scope.isRequester = function() {
                return $scope.globals.currentUser.roles.indexOf('requester') != -1;
            };
            
            $scope.isScientificCouncil = function() {
                return $scope.globals.currentUser.roles.indexOf('scientific_council') != -1;
            };

            $scope.isLabuser = function() {
                return $scope.globals.currentUser.roles.indexOf('lab_user') != -1;
            }
            
}]);

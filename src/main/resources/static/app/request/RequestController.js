(function(angular) {

    var RequestController = function($rootScope, $scope, $modal, $location,
            Request, RequestAttachment, RequestComment,
            ApprovalComment, ApprovalVote,
            FlowOptionService, $routeParams) {

        $scope.error = "";

        $scope.login = function() {
            $location.path("/login");
        }

        console.log("globals: "+ JSON.stringify($rootScope.globals));

        if ($routeParams.requestId) {
            Request.get({id:$routeParams.requestId}, function (req) {
                $scope.request = req;
            });
        }

        $scope.flow_options = function(options) {
            return FlowOptionService.get_default(options);
        };

        Request.query().$promise.then(function(response) {
            $scope.requests = response ? response : [];
            $scope.displayedCollection = [].concat($scope.requests);
        }, function(response) {
            if (response.data) {
                $scope.error = response.data.message + "\n";
                if (response.data.error == 302) {
                    $scope.accessDenied = true;
                }
            } else {
                $scope.login();
            }
        });

        $scope.fileuploadsuccess = function(request, data) {
            result = new Request(JSON.parse(data));
            //$scope.refresh(request, result);
            request.attachments = result.attachments;
            request.agreementAttachments = result.agreementAttachments;
        };

        $scope.start = function() {
            new Request().$save(function(request) {
                $scope.requests.unshift(request);
                $scope.edit(request);
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.refresh = function(request, result) {
            console.log("Updating request at index: " + $scope.requests.indexOf(request));
            $scope.requests[$scope.requests.indexOf(request)] = result;
            $scope.request = result;
        };

        $scope.update = function(request) {
            request.$update(function(result) {
                $scope.refresh(request, result);
                $scope.editRequestModal.hide();
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.removeAgreementFile = function(f) {
            bootbox.confirm("Are you sure you want to delete file "
                    +  f.name
                    + "?", function(result) {
                if (result) {
                    attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$removeAgreementFile(function(result) {
                        $scope.request.agreementAttachments.splice($scope.request.agreementAttachments.indexOf(f), 1);
                        bootbox.alert("File " + f.name + " deleted.");
                    }, function(response) {
                        $scope.error = response.statusText;
                    });
                }
            });
        };

        $scope.removeFile = function(f) {
            bootbox.confirm("Are you sure you want to delete file "
                    +  f.name
                    + "?", function(result) {
                if (result) {
                    attachment = new RequestAttachment();
                    attachment.requestId = $scope.request.processInstanceId;
                    attachment.id = f.id;
                    attachment.$remove(function(result) {
                        $scope.request.attachments.splice($scope.request.attachments.indexOf(f), 1);
                        bootbox.alert("File " + f.name + " deleted.");
                    }, function(response) {
                        $scope.error = response.statusText;
                    });
                }
            });
        };

        $scope.remove = function(request) {
            bootbox.confirm("Are you sure you want to delete request "
                    +  request.processInstanceId
                    + "?", function(result) {
                if (result) {
                    request.$remove(function(result) {
                        $scope.requests.splice($scope.requests.indexOf(request), 1);
                        bootbox.alert("Request " + request.processInstanceId + " deleted.");
                    }, function(response) {
                        $scope.error = response.statusText;
                    });
                }
            });
        };

        $scope.submitRequest = function(request) {
            bootbox.confirm(
                "Are you sure you want to submit the request?\n "
                + "After submission the request cannot be edited anymore.",
                function(confirmed) {
                    if (confirmed) {
                        request.$submit(function(result) {
                            $scope.request = result;
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $scope.error = $scope.error + response.data.message + "\n";
                        });
                    }
                });
        }

        $scope.submitForApproval = function(request) {
            bootbox.confirm(
                "Are you sure you want to submit the request for approval?", 
                function(confirmed) {
                    if (confirmed) {
                        request.$submitForApproval(function(result) {
                            $scope.request = result;
                            $scope.editRequestModal.hide();
                        }, function(response) {
                            $scope.error = $scope.error + response.data.message + "\n";
                        });
                    }
                });
        }
        
        $scope.view = function(request) {
            $location.path("/request/view/" + request.processInstanceId);
        };

        $scope.edit = function(request) {
            if ($scope.viewRequestModal) {
                $scope.viewRequestModal.hide();
            }

            if (request) {

                Request.get({id:request.processInstanceId}, function (data) {
                    $scope.request = data;
                    if (!($scope.globals.currentUser.userid in $scope.request.approvalVotes)) {
                        $scope.request.approvalVotes[$scope.globals.currentUser.userid] = 
                        new ApprovalVote({value: 'NONE'});
                    }
                    $scope.edit_comment = {};
                    $scope.comment_edit_visibility = {};
                    if (data.returnDate == null) {
                        data.returnDate = new Date();
                    }
                    $scope.editRequestModal = $modal({id: 'editRequestWindow', scope: $scope, template: '/app/request/editrequest.html'});
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
                $scope.requests[$scope.requests.indexOf(request)] = result;
            }, function(response) {
                $scope.error = response.statusText;
            });
        }

        $scope.unclaim = function(request) {
            request.$unclaim(function(result) {
                $scope.requests[$scope.requests.indexOf(request)] = result;
            }, function(response) {
                $scope.error = response.statusText;
            });
        };

        $scope.focus = function (el) {
            $(el).focus();
        }

        $scope.addComment = function(request, body) {
            comment = new RequestComment(body);
            comment.processInstanceId = request.processInstanceId;
            comment.$save(function(result) {
                request.comments.push(result);
                $scope.edit_comment = {};
            }, function(response) {
                $scope.error = response.statusText;
            });
        }

        $scope.updateComment = function(request, body) {
            comment = new RequestComment(body);
            comment.$update(function(result) {
                index = $scope.request.comments.indexOf(body);
                //console.log("Updating comment at index " + index);
                $scope.request.comments[index] = result;
                $scope.comment_edit_visibility[comment.id] = 0;
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.removeComment = function(comment) {
            new RequestComment(comment).$remove(function(result) {
                $scope.request.comments.splice(
                        $scope.request.comments.indexOf(comment), 1);
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.addApprovalComment = function(request, body) {
            comment = new ApprovalComment(body);
            comment.processInstanceId = request.processInstanceId;
            comment.$save(function(result) {
                request.approvalComments.push(result);
                $scope.approval_comment = {};
            }, function(response) {
                $scope.error = response.statusText;
            });
        }
        
        $scope.updateApprovalComment = function(request, body) {
            comment = new ApprovalComment(body);
            comment.$update(function(result) {
                index = $scope.request.approvalComments.indexOf(body);
                //console.log("Updating comment at index " + index);
                $scope.request.approvalComments[index] = result;
                $scope.approval_comment_edit_visibility[comment.id] = 0;
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.updateVote = function(request, value) {
            vote = new ApprovalVote();
            vote.value = value;
            vote.processInstanceId = request.processInstanceId;
            vote.$save(function(result) {
                $scope.request.approvalVotes[$scope.globals.currentUser.userid] = result;
                //console.log("Updating vote");
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };
        
        $scope.removeComment = function(comment) {
            new ApprovalComment(comment).$remove(function(result) {
                $scope.request.approvalComments.splice(
                        $scope.request.approvalComments.indexOf(comment), 1);
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };
                
        $scope.getName = function(user) {
            if (user == null) {
                return "";
            }
            return user.firstName
                + ((user.firstName=="" || user.lastName=="" || user.lastName == null ) ? "" : " ")
                + (user.lastName==null ? "" : user.lastName);
        }
        
        $scope.size = function(obj) {
            var size = 0, key;
            for (key in obj) {
                if (obj.hasOwnProperty(key)) size++;
            }
            return size;
        };

    };

    RequestController.$inject = [ '$rootScope', '$scope', '$modal', '$location',
                                  'Request', 'RequestAttachment', 'RequestComment',
                                  'ApprovalComment', 'ApprovalVote',
                                  'FlowOptionService', '$routeParams'];
    angular.module("ProcessApp.controllers").controller("RequestController",
            RequestController);

    var TaskController = function($scope, Task, FlowOptionService) {
        Task.query(function(response) {
            $scope.tasks = response ? response : [];
        });
        Task.queryCompleted(function(response) {
            $scope.completed_tasks = response ? response : [];
        });

        $scope.complete = function(task) {
            task.attachments = [];
            task.$update(function() {
                $('#task_' + task.id).removeClass('panel-default').addClass(
                        'panel-info');
                task.completed = true;
            });
        };

        $scope.flow_options = function(options) {
            return FlowOptionService.get_default(options);
        };
    };
    TaskController.$inject = [ '$scope', 'Task', 'FlowOptionService' ];
    angular.module("ProcessApp.controllers").controller("TaskController",
            TaskController);

    var FormDataController = function($scope, FormFata) {

        $scope.update = function(formData) {
            alert(formData.name + ': ' + formData.value);
            formData.$update();
        };
    };
    FormDataController.$inject = [ '$scope', 'FormData' ];
    angular.module("ProcessApp.controllers").controller("FormDataController",
            FormDataController);

}(angular));

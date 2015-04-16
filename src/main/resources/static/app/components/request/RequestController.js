(function(angular) {

    var RequestController = function($rootScope, $scope, $modal, $location, Request, RequestAttachment, Task, FlowOptionService) {
        
        $scope.error = "";
        
        $scope.login = function() {
            $location.path("/login");
        }

        console.log("globals: "+ JSON.stringify($rootScope.globals));
        
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

        $scope.fileuploadsuccess = function(data) {
            result = JSON.parse(data);
            $scope.refresh($scope.request, result);
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
            $scope.request = result;
            $scope.requests[$scope.requests.indexOf(request)] = result;
        }
        
        $scope.update = function(request) {
            request.$update(function(result) {
                $scope.refresh(request, result);
                $scope.editRequestModal.hide();
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
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

        $scope.view = function(request) {
            if ($scope.editRequestModal) {
                $scope.editRequestModal.hide();
            }
            $scope.request = request;
            $scope.viewRequestModal = $modal({scope: $scope, template: '/app/components/request/request.html'});
        };
       
        $scope.edit = function(request) {
            if ($scope.viewRequestModal) {
                $scope.viewRequestModal.hide();
            }
            $scope.request = request;
            if (request.returnDate == null) {
                request.returnDate = new Date();
            }
            $scope.editRequestModal = $modal({id: 'editRequestWindow', scope: $scope, template: '/app/components/request/editrequest.html'});
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
        }
        
        $scope.getName = function(user) {
            if (user == null) {
                return "";
            }
            return user.firstName 
                + ((user.firstName=="" || user.lastName=="" || user.lastName == null ) ? "" : " ") 
                + (user.lastName==null ? "" : user.lastName);
        }

    };
    RequestController.$inject = [ '$rootScope', '$scope', '$modal', '$location', 'Request', 'RequestAttachment', 'Task', 'FlowOptionService' ];
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
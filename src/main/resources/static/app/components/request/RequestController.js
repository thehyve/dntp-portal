(function(angular) {

    var RequestController = function($rootScope, $scope, $modal, $location, Request, Task) {
        
        $scope.error = "";
        
        $scope.login = function() {
            $location.path("/login");
        }

        console.log("globals: "+ JSON.stringify($rootScope.globals));
        
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

        $scope.start = function() {
            new Request().$save(function(request) {
                $scope.requests.unshift(request);
                $scope.edit(request);
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };

        $scope.update = function(request) {
            request.$update(function(result) {
                $scope.request = result;
                $scope.editRequestModal.hide();
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        };
        
        $scope.submitRequest = function(request) {
            request.$submit(function(result) {
                $scope.request = result;
                $scope.editRequestModal.hide();
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
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
            $scope.editRequestModal = $modal({scope: $scope, template: '/app/components/request/editrequest.html'});
        };
        
        $scope.open = function($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.opened = true;
        };
        
        $scope.remove = function(request) {
            request.$remove(function(result) {
                $scope.requests.splice($scope.requests.indexOf(request), 1);
            }, function(response) {
                $scope.error = response.statusText;
            });
        }

        $scope.claim = function(request) {
            request.$claim(function(result) {
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
    RequestController.$inject = [ '$rootScope', '$scope', '$modal', '$location', 'Request', 'Task' ];
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
(function(angular) {

    var ProcessController = function($scope, Process) {
        Process.query(function(response) {
            $scope.processes = response ? response : [];
        });
        Process.queryCompleted(function(response) {
            $scope.completed_processes = response ? response : [];
        });
        
        $scope.start = function() {
            new Process().$save(function(process) {
                $scope.processes.push(process);
            });
        };

        $scope.suspend = function(process) {
            process.suspended = true;
            process.$update();
        };

        $scope.resume = function(process) {
            process.suspended = false;
            process.$update();
        };

        $scope.stop = function(process) {
            process.$remove(function() {
                process.ended = true;
                $('#process_' + process.id).addClass('disabled');
            });
        };
    };
    ProcessController.$inject = [ '$scope', 'Process' ];
    angular.module("ProcessApp.controllers").controller("ProcessController",
            ProcessController);

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
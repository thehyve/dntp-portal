(function(angular) {

    var ProcessFactory = function($resource) {
        return $resource('/processes/:id', {
            id : '@id'
        }, {
            queryCompleted : {
                url : '/processes/completed/',
                method : "GET",
                isArray : true
            },
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };
    ProcessFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Process", ProcessFactory);
    
    var TaskFactory = function($resource) {
        return $resource('/tasks/:id', {
            id : '@id'
        }, {
            queryCompleted : {
                url : '/tasks/completed/',
                method : "GET",
                isArray : true
            },
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };
    TaskFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Task", TaskFactory);
    
    var FormDataFactory = function($resource) {
        return $resource('/formdata/:id', {
            id : '@id'
        }, {
            update : {
                method : "PUT"
            }
        });
    };
    FormDataFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("FormData", FormDataFactory);

    var FlowOptionService = function($cookies) {
        return {
            get_default: function(options) {
                options.headers = function (file, chunk, isTest) {
                    var csrftoken = $cookies['XSRF-TOKEN'];
                    console.log("csrftoken: " + csrftoken);
                    return {
                        'X-CSRFToken': csrftoken,
                        'X-XSRF-TOKEN': csrftoken
                    };
                };
                options.testChunks = false;
                return options;
            }
        };
    };
    FlowOptionService.$inject = [ '$cookies' ];
    angular.module("ProcessApp.services").factory("FlowOptionService", FlowOptionService);
    
}(angular));
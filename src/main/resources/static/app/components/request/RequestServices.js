(function(angular) {

    RequestFactory = function($resource) {
        return $resource('/requests/:id', {
            id : '@processInstanceId'
        }, {
            update : {
                method : "PUT"
            },
            submit : {
                url : '/requests/:id/submit',
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            },
            claim : {
                url : '/requests/:id/claim',
                method : "PUT"
            },
            unclaim : {
                url : '/requests/:id/unclaim',
                method : "PUT"
            },
            removeFile : {
                url : '/requests/:id/files',
                method : "PUT"
            }
        });
    };
    RequestFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Request", RequestFactory);
    
    RequestAttachmentFactory = function($resource) {
        return $resource('/requests/:requestId/files/:id', {
            requestId: '@requestId', 
            id : '@id'
        }, {
            remove : {
                method : "DELETE"
            },
            removeAgreementFile : {
                url : '/requests/:requestId/agreementFiles/:id',
                method : "DELETE"
            }
        
        });
    };
    RequestAttachmentFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("RequestAttachment", RequestAttachmentFactory);
    
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
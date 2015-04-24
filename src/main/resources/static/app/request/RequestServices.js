(function(angular) {

    RequestFactory = function($resource) {

        var _requestFactory = $resource('/requests/:id', {
            id : '@processInstanceId'
        }, {
            update : {
                method : "PUT"
            },
            submit : {
                url : '/requests/:id/submit',
                method : "PUT"
            },
            submitForApproval : {
                url : '/requests/:id/submitForApproval',
                method : "PUT"
            },
            finalise : {
                url : '/requests/:id/finalise',
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
            }
        });

        _requestFactory.isMaterialNeeded = function (request) {
            return request.materialsRequest ? true : false;
        };

        _requestFactory.convertRequestTypeToOpts = function (request) {
            var _type = request.type;
            var cases = {
                1 : function() {
                    request.statisticsRequest = true; //T
                    request.excerptsRequest = false;
                    request.paReportRequest = false;
                    request.materialsRequest = false;
                },
                2 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = true; //T
                    request.paReportRequest = false;
                    request.materialsRequest = false;
                },
                3 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = true; //T
                    request.paReportRequest = true; //T
                    request.materialsRequest = false;
                },
                4 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = true; //T
                    request.paReportRequest = false; //T
                    request.materialsRequest = true;
                },
                5 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = true ;//T
                    request.paReportRequest = true; //T
                    request.materialsRequest = true; //T
                },
                6 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = false;
                    request.paReportRequest = true; //T
                    request.materialsRequest = false;
                },
                7 : function() {
                    request.statisticsRequest = false;
                    request.excerptsRequest = false;
                    request.paReportRequest = false;
                    request.materialsRequest = true;//T
                }
            };

            if (cases[_type]) {
                cases[_type]();
            }

            return request;
        };

        _requestFactory.convertRequestOptsToType = function (request) {

            var mapTypeToOpts = [
                {
                    statisticsRequest : true, //T
                    excerptsRequest : false,
                    paReportRequest : false,
                    materialsRequest : false
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : true, //T
                    paReportRequest : false,
                    materialsRequest : false
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : true, //T
                    paReportRequest : true, //T
                    materialsRequest : false
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : true, //T
                    paReportRequest : false, //T
                    materialsRequest : true
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : true ,//T
                    paReportRequest : true, //T
                    materialsRequest : true //T
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : false,
                    paReportRequest : true, //T
                    materialsRequest : false
                },
                {
                    statisticsRequest : false,
                    excerptsRequest : false,
                    paReportRequest : false,
                    materialsRequest : true//T
                }
            ];

            var requestOptsObj = {
                'statisticsRequest': request.statisticsRequest,
                'excerptsRequest': request.excerptsRequest,
                'paReportRequest': request.paReportRequest,
                'materialsRequest': request.materialsRequest
            };

            for (var i=0; i<mapTypeToOpts.length; i++) {
                if (JSON.stringify(mapTypeToOpts[i]) == JSON.stringify(requestOptsObj)) {
                    return i+1;
                }
            }
        };

        return _requestFactory;
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
            removeDataFile : {
                url : '/requests/:requestId/dataFiles/:id',
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

    RequestCommentFactory = function($resource) {
        return $resource('/requests/:processInstanceId/comments/:id', {
            processInstanceId: '@processInstanceId',
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };
    RequestCommentFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("RequestComment", RequestCommentFactory);

    ApprovalCommentFactory = function($resource) {
        return $resource('/requests/:processInstanceId/approvalComments/:id', {
            processInstanceId: '@processInstanceId',
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };
    ApprovalCommentFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("ApprovalComment", ApprovalCommentFactory);

    ApprovalVoteFactory = function($resource) {
        return $resource('/requests/:processInstanceId/approvalVotes/:id', {
            processInstanceId: '@processInstanceId',
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };
    ApprovalVoteFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("ApprovalVote", ApprovalVoteFactory);

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

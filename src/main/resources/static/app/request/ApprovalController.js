'use strict';

angular.module('ProcessApp.controllers')
    .controller('ApprovalController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'User', 'Request', 'ApprovalComment', 'ApprovalVote',

        function ($rootScope, $scope, $modal, $location, $route,
                User, Request, ApprovalComment, ApprovalVote) {

            if ($rootScope.globals.currentUser.roles.indexOf('palga') !== -1) {
                User.queryScientificCouncil().$promise.then(function(response) {
                    console.log(response);
                    $scope.scientificCouncil = response ? response : [];
                    $scope.scientificCouncilEmail = '';
                    for (var i=0; i < response.length; i++) {
                        if (response[i].contactData) {
                            if ($scope.scientificCouncilEmail.length > 0) {
                                $scope.scientificCouncilEmail += ', ';
                            }
                            var name = $.trim($rootScope.getName(response[i]));
                            if (name.length > 0) {
                                $scope.scientificCouncilEmail += name 
                                + ' <'
                                + response[i].contactData.email
                                + '>';
                            } else {
                                $scope.scientificCouncilEmail += response[i].contactData.email;
                            }
                        }
                    }
                }, function(response) {
                    if (response.data) {
                        $scope.error = response.data.message + '\n';
                        if (response.data.error === 302) {
                            $scope.accessDenied = true;
                        }
                    }
                });
            }

            $scope.updateVote = function(request, value) {
                var vote = new ApprovalVote();
                vote.value = value;
                vote.processInstanceId = request.processInstanceId;
                vote.$save(function(result) {
                    $scope.request.approvalVotes[$scope.globals.currentUser.userid] = result;
                    //console.log('Updating vote');
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                });
            };

            $scope.addApprovalComment = function(request, body) {
                var comment = new ApprovalComment(body);
                comment.processInstanceId = request.processInstanceId;
                comment.$save(function(result) {
                    request.approvalComments.push(result);
                    $scope.approvalComment = {};
                }, function(response) {
                    $scope.error = response.statusText;
                });
            };

            $scope.updateApprovalComment = function(request, body) {
                var comment = new ApprovalComment(body);
                comment.$update(function(result) {
                    var index = $scope.request.approvalComments.indexOf(body);
                    //console.log('Updating comment at index ' + index);
                    $scope.request.approvalComments[index] = result;
                    $scope.approval_comment_edit_visibility[comment.id] = 0;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                });
            };

            $scope.removeApprovalComment = function(comment) {
                new ApprovalComment(comment).$remove(function(result) {
                    $scope.request.approvalComments.splice(
                        $scope.request.approvalComments.indexOf(comment), 1);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                });
            };
}]);

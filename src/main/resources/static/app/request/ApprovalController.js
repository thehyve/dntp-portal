'use strict';

angular.module('ProcessApp.controllers')
    .controller('ApprovalController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'ApprovalComment', 'ApprovalVote',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, ApprovalComment, ApprovalVote) {

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

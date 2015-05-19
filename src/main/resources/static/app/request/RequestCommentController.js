'use strict';

angular.module('ProcessApp.controllers')
    .controller('RequestCommentController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'RequestAttachment', 'RequestComment',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, RequestAttachment, RequestComment) {

            $scope.addComment = function(request, body) {
                var comment = new RequestComment(body);
                comment.processInstanceId = request.processInstanceId;
                comment.$save(function(result) {
                    request.comments.push(result);
                    $scope.edit_comment = {};
                }, function(response) {
                    $scope.error = response.statusText;
                });
            };

            $scope.updateComment = function(request, body) {
                var comment = new RequestComment(body);
                comment.$update(function(result) {
                    var index = $scope.request.comments.indexOf(body);
                    //console.log('Updating comment at index ' + index);
                    $scope.request.comments[index] = result;
                    $scope.commentEditVisibility[comment.id] = 0;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                });
            };

            $scope.removeComment = function(comment) {
                new RequestComment(comment).$remove(function(result) {
                    $scope.request.comments.splice(
                        $scope.request.comments.indexOf(comment), 1);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                });
            };

}]);

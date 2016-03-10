/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, angular) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('RequestCommentController',['$rootScope', '$scope', '$modal', '$location', '$route',
        'Request', 'RequestAttachment', 'RequestComment',

        function ($rootScope, $scope, $modal, $location, $route,
                  Request, RequestAttachment, RequestComment) {

            $scope.addComment = function(request, body) {
                $scope.dataLoading = true;
                var comment = new RequestComment(body);
                comment.processInstanceId = request.processInstanceId;
                comment.$save(function(result) {
                    request.comments.push(result);
                    $scope.edit_comment = {};
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = response.statusText;
                    $scope.dataLoading = false;
                });
            };

            $scope.updateComment = function(request, body) {
                $scope.dataLoading = true;
                var comment = new RequestComment(body);
                comment.$update(function(result) {
                    var index = $scope.request.comments.indexOf(body);
                    //console.log('Updating comment at index ' + index);
                    $scope.request.comments[index] = result;
                    $scope.commentEditVisibility[comment.id] = 0;
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

            $scope.removeComment = function(comment) {
                $scope.dataLoading = true;
                new RequestComment(comment).$remove(function(result) {
                    $scope.dataLoading = false;
                    $scope.request.comments.splice(
                        $scope.request.comments.indexOf(comment), 1);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

}]);
})(console, angular);

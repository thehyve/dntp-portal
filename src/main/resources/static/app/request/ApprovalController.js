/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, jQuery, angular) {
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
                            var name = jQuery.trim($rootScope.getName(response[i]));
                            if (name.length > 0) {
                                $scope.scientificCouncilEmail += name +
                                    ' <' + response[i].contactData.email + '>';
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
                $scope.dataLoading = true;
                var vote = new ApprovalVote();
                vote.value = value;
                vote.processInstanceId = request.processInstanceId;
                vote.$save(function(result) {
                    $scope.request.approvalVotes[$scope.globals.currentUser.userid] = result;
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

            $scope.addApprovalComment = function(request, body) {
                $scope.dataLoading = true;
                var comment = new ApprovalComment(body);
                comment.processInstanceId = request.processInstanceId;
                comment.$save(function(result) {
                    request.approvalComments.push(result);
                    $scope.approvalComment = {};
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = response.statusText;
                    $scope.dataLoading = false;
                });
            };

            $scope.updateApprovalComment = function(request, body) {
                $scope.dataLoading = true;
                var comment = new ApprovalComment(body);
                comment.$update(function(result) {
                    var index = $scope.request.approvalComments.indexOf(body);
                    //console.log('Updating comment at index ' + index);
                    $scope.request.approvalComments[index] = result;
                    $scope.approval_comment_edit_visibility[comment.id] = 0;
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };

            $scope.removeApprovalComment = function(comment) {
                $scope.dataLoading = true;
                new ApprovalComment(comment).$remove(function(result) {
                    $scope.request.approvalComments.splice(
                        $scope.request.approvalComments.indexOf(comment), 1);
                    $scope.dataLoading = false;
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    $scope.dataLoading = false;
                });
            };
}]);
})(console, jQuery, angular);

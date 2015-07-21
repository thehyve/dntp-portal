'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminUserController',['$scope', '$modal', 'User', 'Role', 'UserRole', 'Lab',
        function ($scope, $modal, User, Role, UserRole, Lab) {

            $scope.error = '';
            $scope.accessDenied = false;
            $scope.visibility = {};

            User.query().$promise.then(function(response) {
                $scope.users = response ? response : [];
                $scope.displayedCollection = [].concat($scope.users);
            }, function(response) {
                if (response.data) {
                    $scope.error = $scope.error + response.data.message + '\n';
                    if (response.data.status === 302 || response.data.status === 403) {
                        $scope.accessDenied = true;
                    }
                }
            });

            Role.query(function(response) {
                $scope.roles = response ? response : [];
            });

            Lab.query(function(response) {
                $scope.labs = response ? response : [];
                $scope.labmap = {};
                for(var i in $scope.labs) {
                    $scope.labmap[$scope.labs[i].id] = $scope.labs[i];
                }
            });

            $scope.update = function(userdata) {
                $scope.dataLoading = true;
                if (userdata.id > 0) {
                    userdata.$update(function(result) {
                        $scope.dataLoading = false;
                        $scope.editerror = '';
                        $scope.editUserModal.hide();
                    }, function(response) {
                        $scope.dataLoading = false;
                        if (response.status === 304) { // not modified
                            //console.log(JSON.stringify(response));
                            $scope.editerror = 'Email address not available.';
                        }
                    });
                } else {
                    var user = new User(userdata);
                    user.$save(function(result) {
                        $scope.dataLoading = false;
                        $scope.editerror = '';
                        $scope.editUserModal.hide();
                        $scope.users.unshift(result);
                    }, function(response) {
                        $scope.dataLoading = false;
                        if (response.status === 304) { // not modified
                            //console.log(JSON.stringify(response));
                            $scope.editerror = 'Email address not available.';
                        }
                    });
                }
            };

            $scope.toggleVisibility = function(user) {
                if (!(user.userId in $scope.visibility)) {
                    $scope.visibility[user.userId] = false;
                }
                $scope.visibility[user.userId] = !$scope.visibility[user.userId];
            };

            $scope.activate = function(user) {
                $scope.dataLoading = true;
                user.$activate(function(result) {
                    $scope.users[$scope.users.indexOf(user)] = result;
                    $scope.dataLoading = false;
                });
            };

            $scope.deactivate = function(user) {
                $scope.dataLoading = true;
                user.$deactivate(function(result) {
                    $scope.users[$scope.users.indexOf(user)] = result;
                    $scope.dataLoading = false;
                });
            };

            $scope.getName = function(user) {
                if (user === null) {
                    return '';
                }
                return user.firstName
                    + ((user.firstName === '' || user.lastName === '' || user.lastName === null ) ? '' : ' ')
                    + (user.lastName === null ? '' : user.lastName);
            };

            $scope.remove = function(user) {
                bootbox.confirm('Are you sure you want to delete user '
                +  $scope.getName(user)
                + '?', function(result) {
                    if (result) {
                        user.$remove(function() {
                            $scope.users.splice($scope.users.indexOf(user), 1);
                            bootbox.alert('User ' + $scope.getName(user) + ' deleted.');
                        });
                    }
                });
            };

            $scope.add = function() {
                $scope.edit(new User({'currentRole': 'requester'}));
            };

            $scope.edit = function(usr) {
                $scope.edituser = usr;
                $scope.editerror = '';
                $scope.editUserModal = $modal({scope: $scope, template: '/app/admin/user/edituser.html'});
            }


        }
    ]
);

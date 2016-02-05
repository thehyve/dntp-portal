(function(console, angular, bootbox) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminUserController',['$rootScope', '$scope', '$alert', '$modal', '$location', 
                                       'User', 'Role', 'UserRole', 'Lab',
        function ($rootScope, $scope, $alert, $modal, $location, 
                User, Role, UserRole, Lab) {

            $rootScope.redirectUrl = $location.path();
            
            $scope.login = function () {
                $location.path('/login');
            };

            if (!$rootScope.globals.currentUser) {
                $scope.login();
            }

            $scope.error = '';
            $scope.accessDenied = false;
            $scope.visibility = {};

            User.query().$promise.then(function(response) {
                $scope.users = response ? response : [];
                $scope.displayedCollection = [].concat($scope.users);
            }, function(err) {
                if (err.status === 403) {
                    $rootScope.errormessage = err.data.message;
                    $scope.login();
                    return;
                }
                $scope.error = err.data.message;
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

            var _error = function (msg) {
                console.log('error: ' + msg);
                $alert({
                    title : 'Error',
                    content : msg,
                    placement : 'top',
                    type : 'danger',
                    show : true,
                    duration : 5
                });
            };

            $scope.update = function(userdata) {
                $scope.dataLoading = true;
                if (!isNaN(parseInt(userdata.id, 10))) {
                    userdata.$update(function(result) {
                        $scope.dataLoading = false;
                        $scope.editerror = '';
                        $scope.editUserModal.hide();
                    }, function(response) {
                        $scope.dataLoading = false;
                        if (response.data) {
                            _error(response.data.message);
                        } else {
                            _error('Error');
                        }
                    });
                } else {
                    var user = new User(userdata);
                    user.$save(function(result) {
                        $scope.dataLoading = false;
                        $scope.editerror = '';
                        $scope.editUserModal.hide();
                        $scope.users.unshift(result);
                        bootbox.alert('User has been added. A password reset mail has been sent to ' + result.username + '.');
                    }, function(response) {
                        $scope.dataLoading = false;
                        if (response.data) {
                            _error(response.data.message);
                        } else {
                            _error('Error');
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
                return user.firstName +
                    ((user.firstName === '' || user.lastName === '' || user.lastName === null ) ? '' : ' ') +
                    (user.lastName === null ? '' : user.lastName);
            };

            $scope.remove = function(user) {
                bootbox.confirm('Are you sure you want to delete user ' +
                        $scope.getName(user) + '?',
                    function(result) {
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
            };

        }
    ]
);
})(console, angular, bootbox);

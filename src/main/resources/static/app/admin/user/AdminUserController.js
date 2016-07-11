/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular, bootbox) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminUserController',['$rootScope', '$scope', '$alert', '$modal', '$location', 
                                       'User', 'Role', 'UserRole', 'Lab','SpecialismService',
        function ($rootScope, $scope, $alert, $modal, $location, 
                User, Role, UserRole, Lab, SpecialismService) {

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

            var _getDisplayLab = function(labId) {
                if (labId in $scope.labmap) {
                    return _.get($scope.labmap, [labId, 'number']) + '. ' +
                        _.get($scope.labmap, [labId, 'name']);
                } else {
                    return '';
                }
            };

            $scope.getDisplayLabsForUser = function(user) {
                if (user.currentRole === 'hub_user') {
                    var labs = _.map(user.hubLabIds, function(labId) {
                        return _getDisplayLab(labId);
                    });
                    return _.compact(labs);
                } else {
                    return [_getDisplayLab(user.labId)];
                }
            };

            $scope.getDisplayName = function(user) {
                if (user === null) {
                    return '';
                }
                return _.compact([user.firstName, user.lastName]).join(' ');
            };

            var _addInfoToUser = function(user) {
                user.displayLabs = $scope.getDisplayLabsForUser(user);
                user.displayName = $scope.getDisplayName(user);
                return user;
            };

            var _loadUsers = function() {
                User.query().$promise.then(function(response) {
                    var users = response ? response : [];
                    $scope.users = _.map(users, function(user) {
                        user = _addInfoToUser(user);
                        return user;
                    });
                    $scope.displayedCollection = [].concat($scope.users);
                });
            };

            var _loadRoles = function() {
                return Role.query(function(response) {
                    $scope.roles = response ? response : [];
                });
            };

            var _loadLabsAndUsers = function() {
                Lab.query(function(response) {
                    $scope.labs = response ? response : [];
                    $scope.labmap = {};
                    for(var i in $scope.labs) {
                        $scope.labmap[$scope.labs[i].id] = $scope.labs[i];
                    }
                    _loadUsers();
                });
            };

            _loadRoles();
            _loadLabsAndUsers();

            $scope.update = function(userdata) {
                if (userdata.currentRole !== 'requester') {
                    userdata.specialism = '';
                }
                userdata.hubLabIds = _.map(userdata.hubLabs, function(lab) { return lab.id; });
                $scope.dataLoading = true;
                if (!isNaN(parseInt(userdata.id, 10))) {
                    userdata.$update(function() {
                        userdata = _addInfoToUser(userdata);
                        $scope.dataLoading = false;
                        $scope.editUserModal.hide();
                        $scope.editUserModal.destroy();
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
                        $scope.editUserModal.destroy();
                        result = _addInfoToUser(result);
                        $scope.users.unshift(result);
                        bootbox.alert(
                                $rootScope.translate('User has been added. A password reset mail has been sent to ?.',
                                        {email: result.username}));
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

            $scope.activate = function(user) {
                $scope.dataLoading = true;
                var usr = new User(user);
                usr.$activate(function(result) {
                    $scope.users[$scope.users.indexOf(user)].active = result.active;
                    $scope.dataLoading = false;
                });
            };

            $scope.deactivate = function(user) {
                $scope.dataLoading = true;
                var usr = new User(user);
                usr.$deactivate(function(result) {
                    $scope.users[$scope.users.indexOf(user)].active = result.active;
                    $scope.dataLoading = false;
                });
            };

            $scope.remove = function(user) {
                bootbox.confirm($rootScope.translate('Are you sure you want to delete user ? ?',
                        {name: $scope.getName(user)}),
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

            $scope.cancelByEscKey = function (key) {
                if (key.keyCode === 27) {
                    $scope.cancel();
                }
            };

            $scope.cancel = function() {
                $scope.editUserModal.hide();
                $scope.editUserModal.destroy();
            };

            $scope.edit = function(usr) {
                $scope.edituser = usr;
                $scope.hubLabs = _.map($scope.labs, function (lab) { lab.disabled = !lab.active; return lab; });
                $scope.edituser.hubLabs = _.map($scope.labs, function(lab) {
                    lab.ticked = _.includes($scope.edituser.hubLabIds, lab.id);
                    return lab;
                });
                $scope.editUserModal = $modal({
                    id: 'editUserWindow',
                    scope: $scope,
                    templateUrl: '/app/admin/user/edituser.html',
                    backdrop: 'static'
                });
            };

            $scope.isPredefinedSpecialism = function (val) {
                return SpecialismService.findPredefined(val) ? true : false;
            }

        }
    ]
);
})(console, _, angular, window.bootbox);


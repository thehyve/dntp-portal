(function(angular) {
	
  var UserController = function($scope, User, Role, UserRole) {
    
    $scope.error = "";
    $scope.accessDenied = false;
    $scope.userRoles = {};
    $scope.visibility = {};
      
    $scope.updateUserRoles = function(users) {
        angular.forEach($scope.users, function(user) {
            $scope.userRoles[user.id] = {};
           angular.forEach(user.roles, function(role) {
               $scope.userRoles[user.id][role.id] = true;
           }) 
        });
    }
    
	User.query().$promise.then(function(response) {
        $scope.users = response ? response : [];
        $scope.updateUserRoles($scope.users);
    }, function(response) {
        $scope.error = $scope.error + response.data.message + "\n";
        if (response.data.error == 302) {
            $scope.accessDenied = true;
        }
    });
	
	Role.query(function(response) {
	    $scope.roles = response ? response : [];
	});
   
    $scope.add = function(userdata) {
        var user = new User(userdata);
          user.$save(function(result) {
              $scope.users = [result].concat($scope.users);
          });
    };
   
    $scope.toggleVisibility = function(user) {
        if (!(user.id in $scope.visibility)) {
            $scope.visibility[user.id] = false;
        }
        $scope.visibility[user.id] = !$scope.visibility[user.id];
    }
    
    $scope.update = function(user) {
    	user.$update();
    };
    
    $scope.activate = function(user) {
        user.$activate(function(result) {
            $scope.users[$scope.users.indexOf(user)] = result;
        });
    }

    $scope.deactivate = function(user) {
        user.$deactivate(function(result) {
            $scope.users[$scope.users.indexOf(user)] = result;
        });
    }
    
    $scope.remove = function(user) {
    	user.$remove(function() {
    	    $scope.users.splice($scope.users.indexOf(user), 1);
    	});    	
    };
    
    $scope.hasRole = function(user, role) {
        for (var i in user.roles) {
            var r = user.roles[i];
            if (role.id==r.id) {
                return true;
            }
        }
        return false;
    };
    
    $scope.toggleRole = function(user, role) {
        if ($scope.userRoles[user.id][role.id] === undefined) {
            $scope.userRoles[user.id][role.id] = false;
        }
        $scope.userRoles[user.id][role.id] = !$scope.userRoles[user.id][role.id];
        $scope.updateRole(user, role);
    }
    
    $scope.updateRole = function(user, role) {
        var userRole = new UserRole({userid: user.id, roleid: role.id});
        if ($scope.userRoles[user.id][role.id]) {
            userRole.$save();
            user.roles[user.roles.length] = role;
        } else {
            userRole.$remove(function(result) {
            }, function(response) {
                alert(response);
            });
            var i = 0;
            for (; i < user.roles.length; i++) {
                if (user.roles[i].id == role.id) {
                    break;
                }
            }
            user.roles.splice(i, 1);
        }
    }
  
  };

  var RoleController = function($scope, User) {
      Role.query(function(response) {
        $scope.roles = response ? response : [];
      });
     
      $scope.add = function(role) {
        role.$save(function(result) {
          $scope.roles.push(result);
        });
      };
     
      $scope.update = function(role) {
          role.$update();
      };

      $scope.remove = function(user) {
          role.$remove(function() {
              $scope.roles.splice($scope.roles.indexOf(role), 1);
          });     
      };
    };

    var LabController = function($scope, Lab) {
        Lab.query(function(response) {
          $scope.labs = response ? response : [];
        });
       
        $scope.add = function(data) {
            var lab = new Lab(data);
          lab.$save(function(result) {
            $scope.labs.push(result);
          });
        };
       
        $scope.update = function(update) {
            lab.$update();
        };

      };

      var InstitutionController = function($scope, Institution) {
          Institution.query(function(response) {
            $scope.institutions = response ? response : [];
          });
         
          $scope.add = function(data) {
              var institution = new Institution(data);
              institution.$save(function(result) {
              $scope.institutions.push(result);
            });
          };
         
          $scope.update = function(institution) {
              institution.$update();
          };

        };
    
    UserController.$inject = [ '$scope', 'User', 'Role', 'UserRole' ];
    angular.module("ProcessApp.controllers").controller("UserController",
            UserController);

    RoleController.$inject = [ '$scope', 'Role' ];
    angular.module("ProcessApp.controllers").controller("RoleController",
            RoleController);

    LabController.$inject = [ '$scope', 'Lab' ];
    angular.module("ProcessApp.controllers").controller("LabController",
            LabController);

    InstitutionController.$inject = [ '$scope', 'Institution' ];
    angular.module("ProcessApp.controllers").controller("InstitutionController",
            InstitutionController);

}(angular));
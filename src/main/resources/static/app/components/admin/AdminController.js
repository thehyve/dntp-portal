(function(angular) {
	
  var UserController = function($scope, $modal, User, Role, UserRole, Lab, Institute) {
    
    $scope.error = "";
    $scope.accessDenied = false;
    $scope.visibility = {};
      
	User.query().$promise.then(function(response) {
        $scope.users = response ? response : [];
        $scope.displayedCollection = [].concat($scope.users);
    }, function(response) {
        $scope.error = $scope.error + response.data.message + "\n";
        if (response.data.error == 302) {
            $scope.accessDenied = true;
        }
    });
	
	Role.query(function(response) {
	    $scope.roles = response ? response : [];
	});
   
	Lab.query(function(response) {
	   $scope.labs = response ? response : []; 
	   $scope.labmap = {};
	   for(i in $scope.labs) {
	       $scope.labmap[$scope.labs[i].id] = $scope.labs[i];
	   }
	});

    $scope.update = function(userdata) {
        if (userdata.id > 0) {
            userdata.$update(function(result) {
                $scope.editUserModal.hide();
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        } else {
            var user = new User(userdata);
            user.$save(function(result) {
                $scope.editUserModal.hide();
                $scope.users.unshift(result);
            }, function(response) {
                $scope.error = $scope.error + response.data.message + "\n";
            });
        }
    };
   
    $scope.toggleVisibility = function(user) {
        if (!(user.userId in $scope.visibility)) {
            $scope.visibility[user.userId] = false;
        }
        $scope.visibility[user.userId] = !$scope.visibility[user.userId];
    }
    
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
    
    $scope.getName = function(user) {
        if (user == null) {
            return "";
        }
        return user.firstName 
            + ((user.firstName=="" || user.lastName=="" || user.lastName == null ) ? "" : " ") 
            + (user.lastName==null ? "" : user.lastName);
    }
    
    $scope.remove = function(user) {
        bootbox.confirm("Are you sure you want to delete user " 
                +  $scope.getName(user)
                + "?", function(result) {
            if (result) {
                user.$remove(function() {
                    $scope.users.splice($scope.users.indexOf(user), 1);
                    bootbox.alert("User " + $scope.getName(user) + " deleted.");
                });
            }
        });    	
    };
    
    $scope.add = function() {
        $scope.edit(new User({'currentRole': 'requester'}));
    };
    
    $scope.edit = function(usr) {
        $scope.edituser = usr;
        $scope.editUserModal = $modal({scope: $scope, template: '/app/components/admin/edituser.html'});
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

    var LabController = function($scope, $modal, Lab) {
        
        $scope.error = "";
        $scope.accessDenied = false;
        $scope.visibility = {};
        
        Lab.query(function(response) {
          $scope.labs = response ? response : [];
        });
       
        $scope.add = function() {
            $scope.edit(new Lab());
        };
       
        $scope.update = function(labdata) {
            if (labdata.id > 0) {
                labdata.$update(function(result) {
                    $scope.editLabModal.hide();
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + "\n";
                });
            } else {
                var lab = new Lab(labdata);
                lab.$save(function(result) {
                    $scope.editLabModal.hide();
                    $scope.labs.unshift(result);
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + "\n";
                });
            }
        };

        $scope.toggleVisibility = function(lab) {
            if (!(lab.id in $scope.visibility)) {
                $scope.visibility[lab.id] = false;
            }
            $scope.visibility[lab.id] = !$scope.visibility[lab.id];
        }
        
        $scope.remove = function(lab) {
            lab.$remove(function() {
                $scope.labs.splice($scope.labs.indexOf(lab), 1);
            });     
        };
        
        $scope.edit = function(lb) {
            $scope.editlab = lb;
            $scope.editLabModal = $modal({scope: $scope, template: '/app/components/admin/editlab.html'});
        };        
        
      };

    UserController.$inject = [ '$scope', '$modal', 'User', 'Role', 'UserRole',
                               'Lab'];
    angular.module("ProcessApp.controllers").controller("UserController",
            UserController);
 
    RoleController.$inject = [ '$scope', 'Role' ];
    angular.module("ProcessApp.controllers").controller("RoleController",
            RoleController);

    LabController.$inject = [ '$scope', '$modal', 'Lab' ];
    angular.module("ProcessApp.controllers").controller("LabController",
            LabController);

}(angular));
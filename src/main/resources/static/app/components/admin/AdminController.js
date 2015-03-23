(function(angular) {
	
  var UserController = function($scope, $modal, User, Role, UserRole, Lab, Institute) {
    
    $scope.error = "";
    $scope.accessDenied = false;
    $scope.visibility = {};
      
	User.query().$promise.then(function(response) {
        $scope.users = response ? response : [];
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
	});

	Institute.query(function(response) {
       $scope.institutes = response ? response : []; 
    });
	
    $scope.add = function(userdata) {
        var user = new User(userdata);
          user.$save(function(result) {
              $scope.users = $scope.users.unshift(result);
          });
    };
   
    $scope.toggleVisibility = function(user) {
        if (!(user.userId in $scope.visibility)) {
            $scope.visibility[user.userId] = false;
        }
        $scope.visibility[user.userId] = !$scope.visibility[user.userId];
    }
    
    $scope.update = function(user) {
    	user.$update();
    	$scope.editUserModal.hide();
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
       
        $scope.update = function(lab) {
            if (lab.id > 0) {
                lab.$update(function(result) {
                    $scope.editLabModal.hide();
                }, function(response) {
                    $scope.error = $scope.error + response.data.message + "\n";
                });
            } else {
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

      var InstituteController = function($scope, $modal, Institute) {
          
          $scope.error = "";
          $scope.accessDenied = false;
          $scope.visibility = {};
          
          Institute.query(function(response) {
            $scope.institutes = response ? response : [];
          });
         
          $scope.add = function() {
              $scope.edit(new Institute());
          };
         
          $scope.update = function(institute) {
              if (institute.id > 0) {
                  institute.$update(function(result) {
                      $scope.editInstituteModal.hide();
                  }, function(response) {
                      $scope.error = $scope.error + response.data.message + "\n";
                  });
              } else {
                  institute.$save(function(result) {
                      $scope.editInstituteModal.hide();
                      $scope.institutes.unshift(result);
                  }, function(response) {
                      $scope.error = $scope.error + response.data.message + "\n";
                  });
              }
          };

          $scope.toggleVisibility = function(institute) {
              if (!(institute.id in $scope.visibility)) {
                  $scope.visibility[institute.id] = false;
              }
              $scope.visibility[institute.id] = !$scope.visibility[institute.id];
          }
          
          $scope.remove = function(institute) {
              institute.$remove(function() {
                  $scope.institutes.splice($scope.institutes.indexOf(institute), 1);
              });     
          };
          
          $scope.edit = function(inst) {
              $scope.editinstitute = inst;
              $scope.editInstituteModal = $modal({scope: $scope, template: '/app/components/admin/editinstitute.html'});
          };

        };
    
    UserController.$inject = [ '$scope', '$modal', 'User', 'Role', 'UserRole',
                               'Lab', 'Institute'];
    angular.module("ProcessApp.controllers").controller("UserController",
            UserController);
 
    RoleController.$inject = [ '$scope', 'Role' ];
    angular.module("ProcessApp.controllers").controller("RoleController",
            RoleController);

    LabController.$inject = [ '$scope', '$modal', 'Lab' ];
    angular.module("ProcessApp.controllers").controller("LabController",
            LabController);

    InstituteController.$inject = [ '$scope', '$modal', 'Institute' ];
    angular.module("ProcessApp.controllers").controller("InstituteController",
            InstituteController);

}(angular));
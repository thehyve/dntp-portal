(function(angular) {

    var UserFactory = function($resource) {
        return $resource('/admin/users/:id', {
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };

    var RoleFactory = function($resource) {
        return $resource('/admin/roles/:id', {
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };

    var UserRoleFactory = function($resource) {
        return $resource('/admin/roles/:userid/:roleid', {
            userid : '@userid',
            roleid : '@roleid'
        }, {
            save : {
                method : "PUT"
            },
            remove : {
                method : "DELETE"
            }
        });
    };

    UserFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("User", UserFactory);

    RoleFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Role", RoleFactory);

    UserRoleFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("UserRole", UserRoleFactory);    
    
}(angular));
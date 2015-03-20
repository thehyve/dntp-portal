(function(angular) {

    var UserFactory = function($resource) {
        return $resource('/admin/users/:id', {
            id : '@id'
        }, {
            update : {
                method : "PUT"
            },
            activate : {
                url : '/admin/users/:id/activate',
                method : "PUT"
            },
            deactivate : {
                url : '/admin/users/:id/deactivate',
                method : "PUT"
            },
            remove : {
                url : '/admin/users/:id/delete',
                method : "PUT"
            },
            register : {
                url : '/register',
                method : 'POST'
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

    var LabFactory = function($resource) {
        return $resource('/admin/labs/:id', {
            id : '@id'
        }, {
            save : {
                method : "POST"
            },
            update : {
                method : "PUT"
            }
        });
    };

    var InstitutionFactory = function($resource) {
        return $resource('/admin/institutions/:id', {
            id : '@id'
        }, {
            save : {
                method : "POST"
            },
            update : {
                method : "PUT"
            }
        });
    };
    
    UserFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("User", UserFactory);

    RoleFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Role", RoleFactory);

    UserRoleFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("UserRole", UserRoleFactory);    

    LabFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Lab", LabFactory);    

    InstitutionFactory.$inject = [ '$resource' ];
    angular.module("ProcessApp.services").factory("Institution", InstitutionFactory);    

}(angular));

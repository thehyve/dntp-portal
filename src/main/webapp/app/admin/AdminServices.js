/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

var UserFactory = function($resource) {
    return $resource('/api/admin/users/:id', {
        id : '@id'
    }, {
        queryScientificCouncil : {
            method : 'GET',
            url : '/api/admin/users/scientific_council',
            isArray : true
        },
        update : {
            method : 'PUT'
        },
        activate : {
            url : '/api/admin/users/:id/activate',
            method : 'PUT'
        },
        deactivate : {
            url : '/api/admin/users/:id/deactivate',
            method : 'PUT'
        },
        remove : {
            url : '/api/admin/users/:id/delete',
            method : 'PUT'
        },
        register : {
            url : '/api/register/users',
            method : 'POST'
        }
    });
};

var RoleFactory = function($resource) {
    return $resource('/api/admin/roles/:id', {
        id : '@id'
    }, {
        update : {
            method : 'PUT'
        },
        remove : {
            method : 'DELETE'
        }
    });
};

var UserRoleFactory = function($resource) {
    return $resource('/api/admin/roles/:userid/:roleid', {
        userid : '@userid',
        roleid : '@roleid'
    }, {
        save : {
            method : 'PUT'
        },
        remove : {
            method : 'DELETE'
        }
    });
};

var LabFactory = function($resource) {
    return $resource('/api/admin/labs/:id', {
        id : '@id'
    }, {
        save : {
            method : 'POST'
        },
        update : {
            method : 'PUT'
        },
        activate : {
            url : '/api/admin/labs/:id/activate',
            method : 'PUT'
        },
        deactivate : {
            url : '/api/admin/labs/:id/deactivate',
            method : 'PUT'
        }
    });
};

var InstituteFactory = function($resource) {
    return $resource('/api/admin/institutes/:id', {
        id : '@id'
    }, {
        save : {
            method : 'POST'
        },
        update : {
            method : 'PUT'
        }
    });
};

UserFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('User', UserFactory);

RoleFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('Role', RoleFactory);

UserRoleFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('UserRole', UserRoleFactory);

LabFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('Lab', LabFactory);

InstituteFactory.$inject = [ '$resource' ];
angular.module('ProcessApp.services').factory('Institute', InstituteFactory);

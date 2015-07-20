'use strict';

(function(angular) {

    
      var LabRequestFactory = function($resource) {
        return $resource('/labrequests/:id', {
            id : '@id'
        }, {
            getDetailed : {
                url : '/labrequests/detailed',
                method : 'GET',
                isArray: true
            },
            accept : {
                url : '/labrequests/:id/accept',
                method : 'PUT'
            },
            reject : {
                url : '/labrequests/:id/reject',
                method : 'PUT'
            },
            sending : {
                url : '/labrequests/:id/sending',
                method : 'PUT'
            },
            received : {
                url : '/labrequests/:id/received',
                method : 'PUT'
            },
            returning : {
                url : '/labrequests/:id/returning',
                method : 'PUT'
            },
            returned : {
                url : '/labrequests/:id/returned',
                method : 'PUT'
            },
            complete : {
                url : '/labrequests/:id/complete',
                method : 'PUT'
            },
            claim : {
                url : '/labrequests/:id/claim',
                method : 'PUT'
            },
            unclaim : {
                url : '/labrequests/:id/unclaim',
                method : 'PUT'
            }
        });
    };

  LabRequestFactory.$inject = [ '$resource' ];
  angular.module('ProcessApp.services').factory('LabRequest', LabRequestFactory);

  var LabRequestCommentFactory = function($resource) {
      return $resource('/labrequests/:labRequestId/comments/:id', {
          labRequestId: '@labRequestId',
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
  LabRequestCommentFactory.$inject = [ '$resource' ];
  angular.module('ProcessApp.services').factory('LabRequestComment', LabRequestCommentFactory);
  
  
}(angular));


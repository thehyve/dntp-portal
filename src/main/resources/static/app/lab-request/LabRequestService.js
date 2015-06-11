'use strict';

(function(angular) {

  var LabRequestFactory = function($resource) {
    return $resource('/labrequests/:id', {
      id: '@id'
    }, {
      accept : {
        url : '/labrequests/:id/accept',
        method : 'PUT'
      },
      reject : {
        url : '/labrequests/:id/reject',
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

}(angular));


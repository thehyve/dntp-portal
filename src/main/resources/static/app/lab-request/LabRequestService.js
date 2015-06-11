'use strict';

(function(angular) {

  var LabRequestFactory = function($resource) {
    return $resource('/labrequests/:id', {
      id: '@id'
    }, {
      accept : {
        url : '/labrequest/:id/accept',
        method : 'PUT'
      },
      reject : {
        url : '/labrequest/:id/reject',
        method : 'PUT'
      }
    });
  };

  LabRequestFactory.$inject = [ '$resource' ];
  angular.module('ProcessApp.services').factory('LabRequest', LabRequestFactory);

}(angular));


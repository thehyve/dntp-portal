'use strict';

(function(angular) {

  var LabRequestFactory = function($resource) {
    return $resource('/labrequests/:tasksId', {
      tasksId: '@taskId'
    }, {
      accept : {
        url : '/labrequest/:tasksId/accept',
        method : 'PUT'
      },
      reject : {
        url : '/labrequest/:tasksId/reject',
        method : 'PUT'
      }
    });
  };

  LabRequestFactory.$inject = [ '$resource' ];
  angular.module('ProcessApp.services').factory('LabRequest', LabRequestFactory);

}(angular));


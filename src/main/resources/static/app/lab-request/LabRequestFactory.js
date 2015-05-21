'use strict';

(function(angular) {

  var LabRequestFactory = function($resource) {
    return $resource('/labrequests', {
      processInstanceId: '@processInstanceId'
    }, {

    });
  };

  LabRequestFactory.$inject = [ '$resource' ];
  angular.module('ProcessApp.services').factory('LabRequest', LabRequestFactory);


}(angular));


'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', ['$rootScope', '$scope', '$modal', '$location', '$route',
    'LabRequest', 'Restangular',

    function ($rootScope, $scope, $modal, $location, $route,
              LabRequest, Restangular) {


      $scope.labRequests = [
        {

          dateCreated : 1000000,
          status : 'PENDING'
        }
      ];

      Restangular.all('labrequests').getList().then(function (labRequests) {

        $scope.labRequests = labRequests;
        console.log(labRequests);

      }, function () {
        console.log("error");
      });

    }]);

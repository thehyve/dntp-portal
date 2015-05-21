'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', ['$rootScope', '$scope', '$modal', '$location', '$route',
    'LabRequest', 'Restangular',

    function ($rootScope, $scope, $modal, $location, $route,
              LabRequest, Restangular) {



      Restangular.all('labrequests').getList().then(function (labRequests) {

        $scope.labRequests = labRequests;
        console.log(labRequests);

      }, function () {
        console.log("error");
      });

      $scope.edit = function (labRequest) {
        console.log(labRequest);
        $scope.labRequest = labRequest;
        $scope.editRequestModal = $modal({id: 'labRequestWindow', scope: $scope, template: '/app/lab-request/lab-request.html', backdrop: 'static'});
      };

      $scope.cancel = function (labRequest) {
        $scope.editRequestModal.hide();
      };

    }]);

'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', ['$rootScope', '$scope', '$modal', '$location', '$route',
    'LabRequest', 'Restangular',

    function ($rootScope, $scope, $modal, $location, $route,
              LabRequest, Restangular) {

      $scope.alerts = [];

      Restangular.all('labrequeests').getList().then(function (labRequests) {

        $scope.labRequests = labRequests;
        ///console.log(labRequests);

      }, function (err) {
        $scope.alerts.push({type: 'danger', msg: 'Error : ' + err.data.status  + ' - ' + err.data.error });
      });

      $scope.edit = function (labRequest) {
        // console.log(labRequest);
        $scope.labRequest = labRequest;
        $scope.editRequestModal = $modal({id: 'labRequestWindow', scope: $scope, template: '/app/lab-request/lab-request.html', backdrop: 'static'});
      };

      $scope.cancel = function (labRequest) {
        $scope.editRequestModal.hide();
      };

      $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
      };

    }]);

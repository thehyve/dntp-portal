'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', ['$rootScope', '$scope', '$modal', '$location', '$route',
    'LabRequest', 'Restangular',

    function ($rootScope, $scope, $modal, $location, $route,
              LabRequest, Restangular) {

      $scope.alerts = [];

      $scope.loadLabRequests = function() {
          Restangular.all('labrequests').getList().then(function (labRequests) {
    
            $scope.labRequests = labRequests;
            ///console.log(labRequests);
    
          }, function (err) {
            $scope.alerts.push({type: 'danger', msg: 'Error : ' + err.data.status  + ' - ' + err.data.error });
          });
      }
      $scope.loadLabRequests();

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

      $scope.reject = function (labRequest) {
        console.log(labRequest);
        LabRequest.reject({id:labRequest.id}, function (result) {
          console.log("after reject", result);
          if ($scope.editRequestModal) {
              $scope.editRequestModal.hide();
          }
          $scope.loadLabRequests();
        });
      };

      $scope.accept = function (labRequest) {
        LabRequest.accept({id:labRequest.id}, function (result) {
          console.log("after accept", result);
          if ($scope.editRequestModal) {
              $scope.editRequestModal.hide();
          }
          $scope.loadLabRequests();
        });

      };

      $scope.isLabUser = function () {
       return $rootScope.globals.currentUser.roles.indexOf('lab_user') != -1
      }

    }]);

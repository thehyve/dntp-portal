'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', ['$rootScope', '$scope', '$modal', '$location', 'Restangular',
    function ($rootScope, $scope, $modal, $location, Restangular) {

      $scope.labReqModal = $modal({
        id: 'labRequestWindow',
        scope: $scope,
        template: '/app/lab-request/lab-request.html',
        backdrop: 'static',
        show: false
      });

      $scope.alerts = [];


      $scope.loadLabRequests = function() {
          Restangular.all('labrequests').getList().then(function (labRequests) {

            $scope.labRequests = labRequests;

          }, function (err) {
            $scope.alerts.push({type: 'danger', msg: 'Error : ' + err.data.status  + ' - ' + err.data.error });
          });
      };

      $scope.loadLabRequests();

      $scope.edit = function (labRequest) {
        $scope.labRequest = labRequest;
        $scope.labReqModal.show();
      };

      $scope.cancel = function () {
        $scope.labReqModal.hide();
      };

      $scope.closeAlert = function (index) {
        $scope.alerts.splice(index, 1);
      };

      $scope.reject = function (labRequest) {
        console.log(labRequest);
        bootbox.prompt({
            title: 'Are you sure you want to reject the lab request?\n<br>' +
            'Please enter a reject reason:',
            callback: function(result) {
                if (result) {
                    labRequest.rejectReason = result;

                  Restangular.one('labrequests', labRequest.id).customPUT(labRequest, 'reject').then(function (d) {
                      console.log("after reject", result);
                      if ($scope.labReqModal) {
                        $scope.labReqModal.hide();
                      }
                      $scope.loadLabRequests();
                    }
                  , function (err) {
                      console.log("Error: ", response);
                    });
                }
            }
        });
      };

      $scope.accept = function (labRequest) {
        Restangular.one('labrequests', labRequest.id).customPUT({}, 'accept').then(function (d) {
            console.log("after accept", result);
            if ($scope.labReqModal) {
              $scope.labReqModal.hide();
            }
            $scope.loadLabRequests();
          }
        );
      };

      $scope.downloadPANumbers = function (labRequest) {
        Restangular.one('labrequests', labRequest.id).customGET('panumbers/csv').then( function (result) {
          console.log("after invoking panumbers/csv");
        });
      };

      $scope.claim = function (labRequest) {
        Restangular.one('labrequests', labRequest.id).customPUT({}, 'claim')
          .then(function (result) {
            console.log("after claim", result);
            $scope.loadLabRequests();
          }
        );
      };

      $scope.unclaim = function (labRequest) {
        Restangular.one('labrequests', labRequest.id).customPUT({}, 'unclaim')
          .then(function (result) {
            console.log("after claim", result);
            $scope.loadLabRequests();
          }
        );

      };

      $scope.isLabUser = function () {
       return $rootScope.globals.currentUser.roles.indexOf('lab_user') != -1
      };

    }]);

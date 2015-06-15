'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', [
       '$rootScope', '$scope', 
       '$modal', 
       '$location', '$route', '$routeParams', 
       'Restangular',
    function (
            $rootScope, $scope, 
            $modal, 
            $location, $route, $routeParams, 
            Restangular) {

      $scope.labReqModal = $modal({
        id: 'labRequestWindow',
        scope: $scope,
        template: '/app/lab-request/edit-lab-request.html',
        backdrop: 'static',
        show: false
      });

      $scope.alerts = [];


      /**
       * To load lab request list
       * @private
       */
      var _loadRequests = function() {
          Restangular.all('labrequests').getList().then(function (labRequests) {
            $scope.labRequests = labRequests;
          }, function (err) {
            $scope.alerts.push({type: 'danger', msg: 'Error : ' + err.data.status  + ' - ' + err.data.error });
          });
      };
      
      /**
       * To load lab request
       * @private
       */
      var _loadRequest = function(labRequest, callback) {
          Restangular.one('labrequests', labRequest.id).get().then(function (result) {
            $scope.labRequest = result;
            callback && callback(result);
          }, function (err) {
            $scope.alerts.push({type: 'danger', msg: 'Error : ' + err.data.status  + ' - ' + err.data.error });
          });
      };

      if ($routeParams.labRequestId) {
          _loadRequest({id: $routeParams.labRequestId});
      } else {
          _loadRequests();
      }
      
      $scope.edit = function (labRequest) {
        _loadRequest(labRequest, function(result) {
            $scope.labReqModal.show();
        });
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
                      _loadRequests();
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
            _loadRequests();
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
            _loadRequests();
          }
        );
      };

      $scope.unclaim = function (labRequest) {
        Restangular.one('labrequests', labRequest.id).customPUT({}, 'unclaim')
          .then(function (result) {
            console.log("after claim", result);
            _loadRequests();
          }
        );

      };

      $scope.isLabUser = function () {
       return $rootScope.globals.currentUser.roles.indexOf('lab_user') != -1
      };

      $scope.enable = function () {

      }

    }]);

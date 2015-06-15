'use strict';

angular.module('ProcessApp.controllers')
  .controller('LabRequestController', [
       '$q','$rootScope', '$scope',
       '$modal',
       '$location', '$route', '$routeParams',
       'Restangular',
    function (
            $q, $rootScope, $scope,
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
      $scope.labRequest = {};

      $scope.getName = function(user) {
          if (user === null) {
              return '';
          }
          return user.firstName +
              ((user.firstName ==='' || user.lastName ==='' || user.lastName === null ) ? '' : ' ') +
              (user.lastName === null ? '' : user.lastName);
      };
      
      /**
       * To load lab request list
       * @private
       */
      var _loadRequests = function() {
          var deferred = $q.defer();
          Restangular.all('labrequests').getList().then(function (labRequests) {
            $scope.labRequests = labRequests;
            deferred.resolve($scope.labRequests);
          }, function (err) {
            deferred.reject("Cannot load lab requests ");
          });
        return deferred.promise;
      };

      /**
       * To load lab request
       * @private
       */
      var _loadRequest = function(labRequest) {
        var deferred = $q.defer();
        Restangular.one('labrequests', labRequest.id).get().then(function (result) {
          $scope.labRequest = result;
          deferred.resolve($scope.labRequest);
        }, function (error) {
          var err_msg = 'Error : ' + err.data.status  + ' - ' + err.data.error;
          deferred.reject(err_msg);
          $scope.alerts.push({type: 'danger', msg: err_msg });
        });
        return deferred.promise;
      };

      if ($routeParams.labRequestId) {
          _loadRequest({id: $routeParams.labRequestId});
      } else {
          _loadRequests();
      }

      $scope.edit = function (labRequest) {
        console.log("about to edit", labRequest);
        _loadRequest(labRequest).then (function (s) {
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
                    labRequest.customPUT(labRequest, 'reject').then(function (d) {
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
        labRequest.customPUT({}, 'accept').then(function (d) {
          console.log("after accept", result);
          if ($scope.labReqModal) {
            $scope.labReqModal.hide();
          }
          _loadRequests();
        });
      };

      $scope.claim = function (labRequest) {
        labRequest.customPUT({}, 'claim')
          .then(function (result) {
            console.log("after claim", result);
            _loadRequests();
          });
      };

      $scope.unclaim = function (labRequest) {
        labRequest.customPUT({}, 'unclaim')
          .then(function (result) {
            console.log("after unclaim", result);
            _loadRequests();
          });
      };

      $scope.isLabUser = function () {
       return $rootScope.globals.currentUser.roles.indexOf('lab_user') != -1
      };

    }]);

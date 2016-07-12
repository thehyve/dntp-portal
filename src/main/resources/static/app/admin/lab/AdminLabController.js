/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(angular, _) {
'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminLabController',['$rootScope', '$scope', '$location', '$modal', '$alert', 'Lab', '$log',
        function ($rootScope, $scope, $location, $modal, $alert, Lab, $log) {

            var _error = function (msg) {
               $log.error('error: ' + msg);
                $alert({
                    title : 'Error',
                    content : msg,
                    placement : 'top',
                    type : 'danger',
                    show : true,
                    duration : 5
                });
            };

            /**
             * From AngularJS v1.5.3, http://angularjs.org
             */
            var EMAIL_REGEXP
                = /^[a-z0-9!#$%&'*+\/=?^_`{|}~.-]+@[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$/i;

            $scope.validateEmail = function(obj) {
                var email = obj.text;
                if (!email) {
                    return false;
                }
                email = email.trim();
                return email.length > 1 && email.length <= 255 && EMAIL_REGEXP.test(email);
            };

            var _loadLabs = function() {
                Lab.query().$promise.then(function(response) {
                    $scope.labs = response ? response : [];
                    $scope.displayedCollection = [].concat($scope.labs);
                }, function(response) {
                    $rootScope.logErrorResponse(response);
                });
            };
            _loadLabs();

            $scope.add = function() {
                $scope.edit(new Lab());
            };

            $scope.update = function(labdata) {
                $scope.dataLoading = true;
                labdata.emailAddresses = _.map(
                        labdata.emailAddressData,
                        function(obj) { return obj.text; });
                if (labdata.id > 0) {
                    labdata.$update(function() {
                        $scope.editLabModal.hide();
                        $scope.editLabModal.destroy();
                        _loadLabs();
                        $scope.dataLoading = false;
                    }, function(response) {
                        _error(response.data.message);
                        $scope.dataLoading = false;
                    });
                } else {
                    var lab = new Lab(labdata);
                    lab.$save(function(result) {
                        $scope.editLabModal.hide();
                        $scope.editLabModal.destroy();
                        $scope.labs.unshift(result);
                        $scope.dataLoading = false;
                    }, function(response) {
                        _error(response.data.message);
                        $scope.dataLoading = false;
                    });
                }
            };

            $scope.activate = function(lab) {
                $scope.dataLoading = true;
                lab.$activate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                    $scope.dataLoading = false;
                });
            };

            $scope.deactivate = function(lab) {
                $scope.dataLoading = true;
                lab.$deactivate(function(result) {
                    $scope.labs[$scope.labs.indexOf(lab)] = result;
                    $scope.dataLoading = false;
                });
            };

            $scope.remove = function(lab) {
                lab.$remove(function() {
                    $scope.labs.splice($scope.labs.indexOf(lab), 1);
                }, function(response) {
                    _error(response.statusText);
                });
            };

            $scope.cancelByEscKey = function (key) {
                if (key.keyCode === 27) {
                    $scope.cancel();
                }
            };

            $scope.cancel = function() {
                $scope.editLabModal.hide();
                $scope.editLabModal.destroy();
            };

            $scope.edit = function(lb) {
                $scope.editlab = _.clone(lb);
                var emailAddresses = _.get(lb, 'emailAddresses', []);
                $scope.editlab.emailAddressData = [].concat(emailAddresses);
                $scope.editLabModal = $modal({
                    id: 'editLabWindow',
                    scope: $scope,
                    templateUrl: '/app/admin/lab/editlab.html',
                    backdrop: 'static'
                });
            };
        }]);
})(angular, _);

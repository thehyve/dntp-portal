'use strict';

angular.module('ProcessApp.services')
    .factory('AgreementFormTemplate', ['$http', '$alert', 'Restangular', '$q',
        function($http, $alert, Restangular, $q) {
            var agreementFormService = {};

            var alertSuccess = function(title, message) {
                $alert({
                    title : title,
                    content : message,
                    placement : 'top-right',
                    type : 'success',
                    show : true,
                    duration : 5
                });
            };

            var alertError = function(message) {
                $alert({
                    title : 'Error',
                    content : message,
                    placement : 'top-right',
                    type : 'danger',
                    show : true,
                    duration : 5
                });
            };

            agreementFormService.get = function() {
                var deferred = $q.defer();
                Restangular.one('public/agreementFormTemplate').get()
                .then(function (response) {
                    var template = response ? response : '';
                    deferred.resolve(template);
                }, function (err) {
                    if (err.status === 403) {
                        deferred.reject(err);
                    } else {
                        console.error(err);
                        alertError(err.response);
                        deferred.reject(err);
                    }
                });
                return deferred.promise;
            };

            agreementFormService.save = function(template) {
                var deferred = $q.defer();
                Restangular.one('admin/agreementFormTemplate').customPUT(template)
                .then(function (response) {
                    var template = response ? response : '';
                    alertSuccess('Template saved.', 'The template has been successfully saved.');
                    deferred.resolve(template);
                }, function (err) {
                    console.error(err);
                    alertError(err.response);
                    deferred.reject(err);
                });
                return deferred.promise;
            };

            return agreementFormService;
    }]);

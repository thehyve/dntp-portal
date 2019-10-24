/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.services')
    .factory('AgreementFormTemplate', ['$http', '$alert', 'Restangular', '$q', '$sanitize',
        function($http, $alert, Restangular, $q, $sanitize) {
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
                Restangular.one('api/public/agreementFormTemplate').get()
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
                Restangular.one('api/admin/agreementFormTemplate').customPUT(template)
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

            agreementFormService.getVariableNames = function(obj) {
                var names = jQuery.map(obj, function(value, key) {
                    if (value instanceof Object) {
                        var subnames = agreementFormService.getVariableNames(value);
                        /*eslint-disable no-unused-vars*/
                        return jQuery.map(subnames, function(v, k) {
                            return key + '.' + v;
                        });
                        /*eslint-enable no-unused-vars*/
                    } else {
                        return key;
                    }
                });
                return names;
            };

            var varsPattern = /{{[\w.]+}}/g;
            var varNamePattern = /{{([\w.]+)}}/;

            var applyMapping = function(template, obj) {
                if (!obj || !template) {
                    return template;
                }
                var contents = template;
                var names = _.uniq(template.match(varsPattern));
                jQuery(names).each(function(i, name) {
                    var varname = name.match(varNamePattern)[1];
                    var varnameRegExp = new RegExp('{{'+varname+'}}', 'g');
                    var value = _.get(obj, varname);
					value = _.escape(value);
                    value = $sanitize(value);
                    contents = contents.replace(varnameRegExp, value);
                });
                return contents;
            };

            /**
             * replaceVariables
             *
             * Whenever scope variables <code>template_var</code> or <code>obj_var</code>
             * change, the value of <code>template_var</code> is copied and in the copy
             * all strings of the form <code>{{var}}</code> (for which
             * <var>var</var> is a key in the scope variable <code>obj_var</code>)
             * are replaced by their value in <code>obj_var</code>.
             * The output is stored in scope variable <code>output_var</code>.
             *
             * @param $scope the angular scope.
             * @param template_var the scope variable used as input, being watched.
             * @param obj_var the scope variable that is used as a replacement map,
             *        being watched.
             * @param output_var the scope variable to which the result is stored.
             */
            agreementFormService.replaceVariables = function($scope, template_var, obj_var, output_var) {
                console.log('Registering variable replacer for ' + template_var);
                $scope.$watch(template_var, function(template) {
                    //console.log(template_var + ' changed.');
                    var obj = _.get($scope, obj_var);
                    var contents = applyMapping(template, obj);
                    $scope[output_var] = contents;
                });
                $scope.$watch(obj_var, function(obj) {
                    //console.log(obj_var + ' changed.');
                    var template = _.get($scope, template_var);
                    var contents = applyMapping(template, obj);
                    $scope[output_var] = contents;
                });
            };

            return agreementFormService;
    }]);

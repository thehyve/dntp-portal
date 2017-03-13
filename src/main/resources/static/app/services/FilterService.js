/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, _, angular) {
'use strict';

angular.module('ProcessApp.services')
    .factory('RequestFilter', [
        function() {
            var filterService = {};

            var _isSuspended = _.matches({reviewStatus: 'SUSPENDED'});
            var _isNotSuspended = _.negate(_isSuspended);
            var _isNotVoted = _.matches({approvalVote: ''});
            var _isVoted = _.negate(_isNotVoted);
            var _approvalStatus = 'Approval';

            filterService.selectVoted = function(requests) {
                return _.chain(requests)
                    .filter(_.matches({status: _approvalStatus}))
                    .filter(_isVoted)
                    .filter(_isNotSuspended)
                    .value();
            };

            filterService.selectNotVoted = function(requests) {
                return _.chain(requests)
                    .filter(_.matches({status: _approvalStatus}))
                    .filter(_isNotVoted)
                    .filter(_isNotSuspended)
                    .value();
            };

            filterService.selectAll = function (requests) {
                return requests;
            };

            filterService.selectSuspended = function (requests) {
                return _.filter(requests, _isSuspended);
            };

            filterService.selectClaimed = function(userid) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_.matches({assignee: userid}))
                        .filter(_isNotSuspended)
                        .value();
                };
            };

            filterService.selectUnclaimed = function (requests) {
                return _.chain(requests)
                    .filter(_.matches({assignee: null}))
                    .filter(_isNotSuspended)
                    .value();
            };

            var _matchesStatus = function(status) {
                switch(status) {
                case 'Approved, waiting for data':
                    return function(request) {
                        return request.status == 'DataDelivery' && (
                            (request.statisticsRequest && request.dataAttachmentCount == 0) ||
                            (!request.statisticsRequest && !request.excerptListUploaded));
                    };
                case 'Data delivered':
                    return function(request) {
                    return request.status == 'DataDelivery' &&
                        (request.statisticsRequest && request.dataAttachmentCount > 0) ||
                        (request.excerptListUploaded &&
                        !(request.paReportRequest || request.materialsRequest || request.clinicalDataRequest));
                    };
                case 'Data delivered, select excerpts':
                    return function(request) {
                        var result = request.status == 'DataDelivery' &&
                            request.excerptListUploaded &&
                            (request.paReportRequest || request.materialsRequest || request.clinicalDataRequest);
                        return result;
                    };
                default:
                    return _.matches({status: status});
                }
            };

            filterService.selectByStatus = function (status) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_matchesStatus(status))
                        .filter(_isNotSuspended)
                        .value();
                };
            };

            return filterService;
    }])
    .factory('LabRequestFilter', [
        function() {
            var filterService = {};

            var _isCompleted = _.matches({status: 'Completed'});
            var _isNotCompleted = _.negate(_isCompleted);

            filterService.selectAll = function (requests) {
                return requests;
            };

            filterService.selectClaimed = function(userid) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_.matches({assignee: userid}))
                        .value();
                };
            };

            filterService.selectUnclaimed = function (requests) {
                return _.chain(requests)
                    .filter(_.matches({assignee: null}))
                    .filter(_isNotCompleted)
                    .value();
            };

            filterService.selectHubAssistanceRequested = function (requests) {
                return _.chain(requests)
                    .filter(_.matches({hubAssistanceRequested: true}))
                    .value();
            }

            filterService.selectByStatus = function (status) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_.matches({status: status}))
                        .value();
                };
            };

            return filterService;
    }])
    .factory('requestStrictFilter', function($filter){
        return function(input, predicate){
            if( 'statusText' in predicate){
                var temp_predicate = {'statusText': predicate['statusText']};
                var temp_results   = $filter('filter')(input, temp_predicate, true);
                delete predicate['statusText'];
                return $filter('filter')(temp_results, predicate);
            } else {
                return $filter('filter')(input, predicate, false);
            }

        }

    });
})(console, _, angular);

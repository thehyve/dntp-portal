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
            var _isNotVoted = _.matches({approvalVote: null});
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

            filterService.selectByStatus = function (status) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_.matches({status: status}))
                        .filter(_isNotSuspended)
                        .value();
                };
            };

            return filterService;
    }])
    .factory('LabRequestFilter', [
        function() {
            var filterService = {};

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
                    .value();
            };

            filterService.selectByStatus = function (status) {
                return function (requests) {
                    return _.chain(requests)
                        .filter(_.matches({status: status}))
                        .value();
                };
            };

            return filterService;
    }]);
})(console, _, angular);

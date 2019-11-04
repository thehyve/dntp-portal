/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */

import angular from 'angular';

angular.module('ProcessApp.services')
    .factory('Upload', ['$http', '$alert', 'FlowOptionService', '$rootScope',
        function($http, $alert, FlowOptionService, $rootScope) {
            var uploadService = {};

            var _units = ['B', 'kB', 'MB', 'GB', 'TB'];

            /**
             * Returns readable string for a filesize specified in bytes (B).
             */
            uploadService.readableFilesize = function(size) {
                var i = 0;
                var s = Number(size);
                while (s >= 1000 && i < _units.length - 1) {
                    s /= 1000;
                    i++;
                }
                return s.toFixed() + ' ' + _units[i];
            };

            var getTimestampString = function() {
                return new Date().getTime().toString();
            };

            /**
             * Generate unique identifier for a file
             * @function
             * @param {FlowFile} file
             * @returns {string}
             */
            var generateUniqueIdentifier = function(file) {
              var relativePath = file.relativePath || file.webkitRelativePath || file.fileName || file.name;
              return file.size + '-' +
                  relativePath.replace(/[^0-9a-zA-Z_-]/img, '') +
                  getTimestampString();
            };

            /*
             * File Operations
             */
            uploadService.flow_options = function(options) {
                //console.log('Init upload service.');
                options.generateUniqueIdentifier = generateUniqueIdentifier;
                return FlowOptionService.get_default(options);
            };

            uploadService.flow_xml_options = function(options) {
                //console.log('Init upload service (XML options).');
                options.generateUniqueIdentifier = generateUniqueIdentifier;
                return FlowOptionService.get_xml_result_options(options);
            };

            uploadService.uploadFile = function(e) {
                e.upload();
            };

            uploadService.fileUploadSuccess = function(type, data, file) {
                var title = $rootScope.translate('Upload success');
                var filetype = $rootScope.translate('filetype_' + type);
                var content = $rootScope.translate('Successfully added filename? (type?).',
                        {filename: file.name, type: filetype});
                $alert({
                    title : title,
                    content : content,
                    placement : 'top-right',
                    type : 'success',
                    show : true,
                    duration : 5
                });
                return content;
            };

            uploadService.fileUploadError = function(data, file, flow) {
                var message = data;
                try {
                    data = JSON.parse(data);
                    message = data.message;
                } catch(e) {
                    // console.log('warning: ' + e);
                }
                console.log('Error uploading \'' + file.name + '\': ' + message);
                var title = $rootScope.translate('Upload failed');
                var content = $rootScope.translate('Failed to upload filename?.', {filename: file.name}) +
                    (message ? '<br>' + message : '');
                $alert({
                    title : title,
                    content : content,
                    placement : 'top-right',
                    type : 'danger',
                    show : true,
                    duration : 5
                });
                flow.cancel();
                return content;
            };

            return uploadService;
    }]);

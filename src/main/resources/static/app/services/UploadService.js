/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
(function(console, angular) {
'use strict';

angular.module('ProcessApp.services')
    .factory('Upload', ['$http', '$alert', 'FlowOptionService',
        function($http, $alert, FlowOptionService) {
            var uploadService = {};

            /*
             * File Operations
             */
            uploadService.flow_options = function(options) {
                //console.log('Init upload service.');
                return FlowOptionService.get_default(options);
            };

            uploadService.flow_xml_options = function(options) {
                //console.log('Init upload service (XML options).');
                return FlowOptionService.get_xml_result_options(options);
            };

            uploadService.uploadFile = function(e) {
                //console.log('Uploading file.');
                e.upload();
            };

            uploadService.fileUploadSuccess = function(type, data, file) {
                //console.log('Upload success: ', file);
                var content = 'Successfully added ' + file.name + ' ('+ type +').';
                $alert({
                    title : 'Upload success',
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
                var content = 'Failed to upload ' + file.name +
                    (message ? '<br>' + message : '');
                $alert({
                    title : 'Upload failed',
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
})(console, angular);

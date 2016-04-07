/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.controllers')
    .controller('ResetPasswordController', ['$scope', '$routeParams', '$timeout', 'Restangular', 
        function ($scope, $routeParams, $timeout, Restangular) {
            'use strict';

            var letters = [
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
                "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
            ];

            var specials = [
                "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+",
                "\\", "|", "{", "}", "[", "]", "/", "?", "<", ">", ",", ".", "`"
            ];

            $scope.password = '';
            $scope.repeatPassword = '';
            $scope.submitted = false;
            $scope.done = false;

            $scope.generateRandomPassword = function() {
                var buffer = new Uint8Array(32);
                asmCrypto.getRandomValues(buffer);
                var pw = asmCrypto.SHA256.base64(buffer);

                // To match password constraints, add a letter, a special and a number:
                buffer = new Uint8Array(3);
                asmCrypto.getRandomValues(buffer);
                var letter = letters[buffer[0] % letters.length];
                var special = specials[buffer[1] % specials.length];
                var number = buffer[2];
                pw = pw + letter + special + number;
                $scope.generatedPassword = pw;
            };

            $scope.submitForm = function () {
                if ($scope.passwordForm.$valid) {
                    $scope.submitted = true;

                    // PUT to server (token and new password)
                    Restangular.one('password').post('reset', { 
                        token: $routeParams.token, 
                        password: $scope.password 
                    }).then(function () {
                        // Notify user
                        $scope.submitted = false;
                        $scope.done = true;
                    }, function restError() {
                        // Error, the token isn't valid!
                        $scope.error = true;
                    });
                }
            };

        }]);

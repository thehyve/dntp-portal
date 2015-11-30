'use strict';

angular.module('ProcessApp.controllers')
    .controller('AgreementFormTemplateController',['$rootScope', '$scope',
                                             '$location',
                                             '$route', '$routeParams',
                                             'Restangular',
                                             '$alert',
    function ($rootScope, $scope,
            $location,
            $route, $routeParams,
            Restangular,
            $alert) {

        $rootScope.redirectUrl = $location.path();

        $scope.login = function () {
            $location.path('/login');
        };

        if (!$rootScope.globals.currentUser) {
            $scope.login();
        }

        $scope.error = '';
        $scope.accessDenied = false;
        $scope.visibility = {};
        $scope.helpTxt = '';

        $scope.loadTemplate = function() {

            Restangular.one('public/agreementFormTemplate').get()
            .then(function (response) {
                $scope.template = response ? response : '';
            },
            function (err) {
                if (err.status === 403) {
                    $rootScope.errormessage = err.data.message;
                    $scope.login();
                    return;
                }
                console.error(err);
                $scope.error = err.data.message;
            });
        };

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

        $scope.saveTemplate = function(template) {
            Restangular.one('admin/agreementFormTemplate').customPUT(template)
            .then(function (response) {
                $scope.template = response ? response : '';
                alertSuccess('Template saved.', 'The template has been successfully saved.');
            }, function (err) {
                console.error(err);
                alertError(err.response);
            });
        };

        $scope.printTemplate = function () {
            console.log('modal print');

            var table = document.querySelector('.markdown-body').innerHTML;
            var myWindow = window.open('', '', 'width=800, height=600');
            myWindow.document.write(table);
            myWindow.print();
        }

        $scope.markdownInfo = function () {
            $location.path('agreementformtemplate/help');
        };

        if ($routeParams.action ===  'help') {
            Restangular.one('app/admin/agreementformtemplate/help.txt').get()
                .then(function (response) {
                    $scope.helpTxt = response;
                },
                function (err) {
                    if (err.status === 403) {
                        $rootScope.errormessage = err.data.message;
                        $scope.login();
                        return;
                    }
                    console.error(err);
                });

        }
        $scope.loadTemplate();

    }
]);

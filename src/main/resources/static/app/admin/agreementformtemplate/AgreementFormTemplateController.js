'use strict';

angular.module('ProcessApp.controllers')
    .controller('AgreementFormTemplateController',['$rootScope', '$scope',
                                             '$location',
                                             '$route', '$routeParams','AgreementFormTemplate',
                                             'Restangular',
                                             '$alert',
    function ($rootScope, $scope,
            $location,
            $route, $routeParams,
              AgreementFormTemplate,
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
            AgreementFormTemplate.get()
            .then(function (template) {
                $scope.template = template;
            }, function (err) {
                if (err.status === 403) {
                    $rootScope.errormessage = err.response;
                    $scope.login();
                    return;
                }
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
            AgreementFormTemplate.save(template)
            .then(function (template) {
                $scope.template = template;
            }, function (err) {
                //
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

'use strict';

angular.module('ProcessApp.controllers')
    .controller('AgreementFormTemplateController', ['$rootScope', '$scope',
        '$location',
        '$route', '$routeParams', 'AgreementFormTemplate',
        function ($rootScope, $scope,
                  $location,
                  $route, $routeParams, AgreementFormTemplate) {

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

        $scope.saveTemplate = function(template) {
            AgreementFormTemplate.save(template)
            .then(function (template) {
                $scope.template = template;
            }, function (err) {
                //
            });
        };

        $scope.printTemplate = function () {
            var myWindow = window.open('', '', 'width=800, height=600');
            myWindow.document.write($scope.template.contents);
            myWindow.print();
        };

        $scope.loadTemplate();

    }
]);

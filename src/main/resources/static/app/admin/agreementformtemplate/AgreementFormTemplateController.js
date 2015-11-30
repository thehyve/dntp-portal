'use strict';

angular.module('ProcessApp.controllers')
    .controller('AgreementFormTemplateController',['$rootScope', '$scope',
                                             '$location',
                                             '$route', '$routeParams',
                                             'AgreementFormTemplate',
                                             '$alert',
    function ($rootScope, $scope,
            $location,
            $route, $routeParams,
            AgreementFormTemplate,
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

        $scope.loadTemplate();

        $scope.saveTemplate = function(template) {
            AgreementFormTemplate.save(template)
            .then(function (template) {
                $scope.template = template;
            }, function (err) {
                //
            });
        };

    }
]);

'use strict';

angular.module('ProcessApp.controllers')
    .controller('AgreementFormTemplateController', ['$rootScope', '$scope',
        '$location',
        '$route', '$routeParams', 'AgreementFormTemplate',
        'taSelection', 'textAngularManager', '$timeout',
        function ($rootScope, $scope,
                  $location,
                  $route, $routeParams, AgreementFormTemplate,
                  taSelection, textAngularManager, $timeout) {

        $rootScope.redirectUrl = $location.path();

        $scope.login = function () {
            $location.path('/login');
        };

        if (!$rootScope.globals.currentUser) {
            $scope.login();
        }

        $scope.isPalga = function() {
            return $scope.globals.currentUser.roles.indexOf('palga') !== -1;
        };

        if (!$scope.isPalga()) {
            $rootScope.errormessage = 'Access is denied';
            return $scope.login();
        }

        $scope.error = '';
        $scope.accessDenied = false;
        $scope.visibility = {};

        var now = new Date();

        $scope.request = {
            requestNumber: '2015-1234',
            requesterName: 'A. Requester',
            requesterEmail: 'requester@dntp.thehyve.nl',
            lab: {
                number: 100,
                name: 'Testlab'
            },
            contactPersonName: 'Principal Investigator',
            title: 'Example request',
            background: "Background of the request.",
            researchQuestion: "Example question.",
            hypothesis: "Example hypothesis.",
            methods: "Example methods.",
            pathologistName: "D.R. Pathologist",
            pathologistEmail: "pathologist@dntp.thehyve.nl",
            billingAddress: {
                telephone: null,
                email: 'req@dntp.thehyve.nl',
                address1: null,
                address2: null,
                postalCode: null,
                city: null,
                stateProvince: null,
                country: 'NL'
            },
            chargeNumber: '12345678',
            researchNumber: '11111-22222',
            date: now.getDate() + '-' + now.getMonth() + '-' + now.getFullYear()
        };

        $scope.variableNames = AgreementFormTemplate.getVariableNames($scope.request);

        AgreementFormTemplate.replaceVariables($scope, 'template.contents', 'request', 'template_contents');

        $scope.insertVariable = function(varname) {
            var editor = jQuery('#editor');
            var element = taSelection.getSelectionElement();
            var e = jQuery(element);
            var result = editor.find(e);
            if (result.length > 0) {
                taSelection.insertHtml('<span>{{'+varname+'}}</span>', element);
            }
            $timeout(function(){
                textAngularManager.refreshEditor('template-editor');
            });
        };
        
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
            window.print();
        };

        $scope.loadTemplate();

    }
]);

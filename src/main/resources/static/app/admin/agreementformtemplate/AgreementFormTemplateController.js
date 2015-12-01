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

        $scope.request = {
            requestNumber: '2015-1234',
            requesterName: 'A. Requester',
            requesterEmail: 'requester@dntp.thehyve.nl',
            lab: {
                number: 100,
                name: 'Testlab',
                contactData: {
                    telephone: null,
                    email: 'lab100@dntp.thehyve.nl',
                    address1: null,
                    address2: null,
                    postalCode: null,
                    city: null,
                    stateProvince: null,
                    country: 'NL'
                }
            },
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
            researchNumber: '11111-22222'
        };

        AgreementFormTemplate.replaceVariables($scope, 'template.contents', 'request', 'template_contents');

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
            var _contents = '<html><head><link rel="stylesheet" type="text/css" href="css/print.css" />' +
                '</head><body onload="window.print()">'
                    .concat($scope.template.contents)
                    .concat('</body></html>');
            myWindow.document.write(_contents);
            myWindow.document.close();
        };

        $scope.loadTemplate();

    }
]);

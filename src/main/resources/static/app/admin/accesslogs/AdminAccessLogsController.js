'use strict';

angular.module('ProcessApp.controllers')
    .controller('AdminAccessLogsController',['$rootScope', '$scope', 
                                             '$location',
                                             '$route', '$routeParams',
                                             'Restangular',
    function ($rootScope, $scope, 
            $location, 
            $route, $routeParams,
            Restangular) {

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

        $scope.loadLogfile = function(filename) {
            Restangular.one('admin/accesslogs/' + filename).get()
            .then(function (response) {
                $scope.logfilename = filename;
                $scope.accesslog = response ? response : [];
            },
            function (err) {
                $scope.error = err.data.message;
            });
        };

        var _loadData = function() {
            Restangular.one('admin/accesslogs').get()
            .then(function (response) {
                $scope.logfiles = response ? response : [];
                if ($routeParams.filename) {
                    $scope.loadLogfile($routeParams.filename);
                } else {
                    $scope.loadLogfile('dntp-access.log');
                }
            },
            function (err) {
                if (err.status === 403) {
                    $rootScope.errormessage = err.data.message;
                    $scope.login();
                    return;
                }
                $scope.error = err.data.message;
            });
        };
        _loadData();
    }
]);

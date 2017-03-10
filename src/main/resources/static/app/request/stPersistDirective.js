/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file LICENSE).
 */
angular.module('ProcessApp.directives')
    .directive('stPersist', function () {
        return {
            require: '^stTable',
            link: function (scope, element, attr, ctrl) {
                var nameSpace = attr.stPersist;
                scope.persistKey = nameSpace;
                var ready = false;

                //save the table state every time it changes
                scope.$watch(function () {
                    return ctrl.tableState();
                }, function (newValue, oldValue) {
                    if (ready) {
                        console.log('Persisting table state = ' + JSON.stringify(newValue));
                        localStorage.setItem(nameSpace, JSON.stringify(newValue));
                    }
                }, true);

                setTimeout(function(){
                    //fetch the table state when the directive is loaded
                    if (localStorage.getItem(nameSpace)) {
                        console.log('Loading table state = ' + localStorage.getItem(nameSpace));
                        var savedState = JSON.parse(localStorage.getItem(nameSpace));
                        var tableState = ctrl.tableState();

                        angular.extend(tableState, savedState);
                        ctrl.pipe();
                    }
                    ready = true;
                }, 0);
            }
        };
    });

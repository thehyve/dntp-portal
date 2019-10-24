import angular from 'angular';

angular.module('ProcessApp.directives')
    .directive('stPersistedSearch', ['stConfig', '$timeout', '$parse', function (stConfig, $timeout, $parse) {

        var persistState = function(key, state) {
            // console.log('Persisting table state = ' + JSON.stringify(state));
            localStorage.setItem(key, JSON.stringify(state));
        };

        var getValue = function(key, field) {
            var state = JSON.parse(localStorage.getItem(key));
            var val = _.get(state, 'search.predicateObject.' + field, '');
            return val;
        };

        var getPersistKey = function(scope) {
            return scope.persistKey || 'st-persist';
        };

        return {
            require: '^stTable',
            link: function (scope, element, attr, ctrl) {
                var tableCtrl = ctrl;
                var promise = null;
                var throttle = attr.stDelay || stConfig.search.delay;
                var event = attr.stInputEvent || stConfig.search.inputEvent;

                attr.$observe('stPersistedSearch', function (newValue, oldValue) {
                    var input = element[0].value;
                    if (newValue !== oldValue && input) {
                        ctrl.tableState().search = {};
                        tableCtrl.search(input, newValue);
                    }
                });

                //table state -> view
                /*eslint-disable no-unused-vars*/
                scope.$watch(function () {
                    return ctrl.tableState();
                }, function (newValue, oldValue) {
                    var predicateExpression = attr.stPersistedSearch || '$';
                    if (newValue.predicateObject && $parse(predicateExpression)(newValue.predicateObject) !== element[0].value) {
                        element[0].value = $parse(predicateExpression)(newValue.predicateObject) || '';
                    }
                }, true);
                /*eslint-enable no-unused-vars*/

                // view -> table state
                element.bind(event, function (evt) {
                    evt = evt.originalEvent || evt;
                    if (promise !== null) {
                        $timeout.cancel(promise);
                    }

                    promise = $timeout(function () {
                        tableCtrl.search(evt.target.value, attr.stPersistedSearch || '');
                        // console.log('Persisting table state for scope: ' + getPersistKey(scope));
                        persistState(getPersistKey(scope), tableCtrl.tableState());
                        promise = null;
                    }, throttle);
                });

                setTimeout(function () {
                    //do this after view has loaded :)
                    var field = attr.stPersistedSearch || '$';
                    var initialValue = getValue(getPersistKey(scope), field);
                    element.val(initialValue);
                }, 0);
            }
        };
    }]);

angular.module('ProcessApp.directives')
  .directive('stPersistedSearch', ['stConfig', '$timeout','$parse', function (stConfig, $timeout, $parse) {
    'use strict';

    var persistState = function(state) {
      console.log('Persisting: ' + JSON.stringify(state));
      localStorage.setItem('temp', JSON.stringify(state));
    };

    var getValue = function(field) {
        var state = JSON.parse(localStorage.getItem('temp'));
        var val = _.get(state, 'search.predicateObject.' + field, '');
        console.log('Set ' + field + ' = ' + val);
        return val;
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
        scope.$watch(function () {
          return ctrl.tableState();
        }, function (newValue, oldValue) {
          var predicateExpression = attr.stPersistedSearch || '$';
          if (newValue.predicateObject && $parse(predicateExpression)(newValue.predicateObject) !== element[0].value) {
            element[0].value = $parse(predicateExpression)(newValue.predicateObject) || '';
          }
        }, true);

        // view -> table state
        element.bind(event, function (evt) {
          evt = evt.originalEvent || evt;
          if (promise !== null) {
            $timeout.cancel(promise);
          }

          promise = $timeout(function () {
            tableCtrl.search(evt.target.value, attr.stPersistedSearch || '');
            persistState(tableCtrl.tableState());
            promise = null;
          }, throttle);
        });

        setTimeout(function(){
            //do this after view has loaded :)
            var field = attr.stPersistedSearch || '$';
            var initialValue = getValue(field);
            element.val(initialValue);
            tableCtrl.search(initialValue, attr.stPersistedSearch || '');
        }, 0);
      }
    };
  }]);

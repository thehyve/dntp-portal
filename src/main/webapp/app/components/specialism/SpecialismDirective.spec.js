xdescribe('specialismComboBox', function() {
    /*eslint-disable no-unused-vars*/
    var $compile,
        $rootScope,
        specialismService;
    /*eslint-enable no-unused-vars*/

    // Load the myApp module, which contains the directive
    beforeEach(() => {
        angular.mock.module('ProcessApp.directives', ['$provide', function($provide) {
        $provide.factory('SpecialismService', function () {
            return {
                getSpecialisms : function() {return []},
                /*eslint-disable no-unused-vars*/
                findPredefined : function (v) {return true},
                /*eslint-enable no-unused-vars*/
                getOther : function () {return 'something'}
            }
        });
    }])});

    // beforeEach(() => angular.module('dntp-templates'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(() => angular.mock.inject(['$_compile', '_$rootScope', 'SpecialismService',
        function(_$compile_, _$rootScope_, SpecialismService) {
        // The injector unwraps the underscores (_) from around the parameter names when matching
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        specialismService = SpecialismService;
    }]));

    it('Replaces the element with the appropriate content', function() {
        // Compile a piece of HTML containing the directive
        var element = null; // $compile("<specialism-combo-box ng-model='foo'></specialism-combo-box>")($rootScope);
        // fire all the watches, so the scope expression {{1 + 1}} will be evaluated
        $rootScope.$digest();
        // Check that the compiled element contains the templated content
        expect(element.html()).toContain("select"); // it should contain 'select' element
        expect(element.html()).toContain("input"); // it should contain 'input' element
    });
});

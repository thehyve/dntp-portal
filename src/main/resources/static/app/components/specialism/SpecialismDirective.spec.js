describe('Unit testing great quotes', function() {
    var $compile,
        $rootScope,
        translate;

    // Load the myApp module, which contains the directive
    beforeEach(module('ProcessApp.directives'));

    beforeEach(module('dntp-templates'));

    // Store references to $rootScope and $compile
    // so they are available to all tests in this describe block
    beforeEach(inject(function(_$compile_, _$rootScope_, $translate){
        // The injector unwraps the underscores (_) from around the parameter names when matching
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        translate = $translate;
    }));

    it('Replaces the element with the appropriate content', function() {
        // Compile a piece of HTML containing the directive
        var element = $compile("<specialism-combo-box ng-model='foo'></specialism-combo-box>")($rootScope);
        // fire all the watches, so the scope expression {{1 + 1}} will be evaluated
        $rootScope.$digest();
        // Check that the compiled element contains the templated content
        expect(element.html()).toContain("select");
    });
});

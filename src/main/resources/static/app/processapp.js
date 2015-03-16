(function(angular) {
	angular.module("ProcessApp.services", []);
    angular.module("ProcessApp.controllers", []);
	angular.module('ProcessApp', [ "flow", 
	                               "ngResource", "ngRoute", "ngCookies",
	                               "pascalprecht.translate",
	                               "ProcessApp.services", "ProcessApp.controllers" ])
	  	.config(function($routeProvider, $translateProvider) {
			$routeProvider.when('/', {
				templateUrl : 'workflow.html',
				controller : ''
			}).when('/login', {
				templateUrl : 'login.html',
				controller : 'NavigationController'
			}).when('/admin', {
			    templateUrl : 'admin.html'
			}).otherwise('/');
			
			$translateProvider.translations('en', messages_en)
			                  .translations('nl', messages_nl);
			$translateProvider.preferredLanguage('en');
	  });

}(angular));
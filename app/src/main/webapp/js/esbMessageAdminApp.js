var esbMessageAdminApp = angular.module('esbMessageAdminApp', 
	[ 
	 	'ui.bootstrap',
	 	'ngRoute', 
	 	'ngGrid', 
	 	'esbMessageAdminControllers',
	 	'esbMessageAdminServices', 
	 	'angucomplete', 
	 	'angular-loading-bar',
	 	'ngQuickDate', 
	 	'MessageCenterModule', 
	 	'ui.layout' 
	]
);

esbMessageAdminApp.config(
	[
		'$routeProvider', 
		function($routeProvider) {
			$routeProvider.when('/errors', 
		    	{
		    		title : "Errors and Messages",
		    		templateUrl : 'partials/errors.html',
		    	}
			).when('/sync', 
				{
					title : "Sync",
					templateUrl : 'partials/sync.html',
				}
			).when('/synckeys', 
				{
					title : "Sync Keys",
					templateUrl : 'partials/synckeys.html',
				}
			).when('/searchkeys', 
				{
					title : "Search Keys",
					templateUrl : 'partials/searchkeys.html',
				}
			).when('/users', 
				{
					title : "Users",
					templateUrl : 'partials/users.html',
				}
			).otherwise(
				{
					redirectTo : '/errors'
				}
			);
		} 
	]
);

esbMessageAdminApp.run(
	[
		'$location',
		'$rootScope',
		function($location, $rootScope) {
		    $rootScope.$on('$routeChangeSuccess', 
		    	function(event, current, previous) {
		        	if (current.$$route) {
		        		$rootScope.title = current.$$route.title;
		        	}
		    	}
		    );
	    } 
	]
);

esbMessageAdminApp.provider('Globals', 
	function() {
	    var self = this;
	
	    // providers are initialized before services
	    // this guarantees that Globals is setup first
	    self.$get = [ '$window', 
	        function($window) {
		        var globals = {
		            'dateFormat' : 
		            {
		                'service' : 'yyyy-MM-ddTHH:mm:ss',
		                'datepicker' : 'yyyy/MM/dd',
		                'timepicker' : 'HH:mm:ss'
		            },
		            'serverSideLogging' : 
		            {
		                'info' : false,
		                'debug' : false,
		                'warn' : true,
		                'error' : true
		            }
		        };
		        return globals;
	    	} 
	    ];
	}
);

String.prototype.supplant = function(o) {
    return this.replace(/{([^{}]*)}/g, 
    	function(a, b) {
        	var r = o[b];
        	return typeof r === 'string' || typeof r === 'number' ? r : a;
    	}
    );
};

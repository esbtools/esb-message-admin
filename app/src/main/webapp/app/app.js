var esbMessageAdminApp = angular.module('esbMessageAdminApp', [
    'ui.bootstrap',
    'ngRoute',
    'ngGrid',
    'angucomplete',
    'angular-loading-bar',
    'ngQuickDate',
    'MessageCenterModule',
    'ui.layout'
]);

esbMessageAdminApp.config(
    [
        '$routeProvider',
        function($routeProvider) {
            $routeProvider.when('/errors', {
                title: "Errors and Messages",
                templateUrl: 'app/error/errors.tpl.html',
            }).when('/sync', {
                title: "Sync",
                templateUrl: 'app/sync/sync.tpl.html',
            }).when('/synckeys', {
                title: "Sync Keys",
                templateUrl: 'app/sync/synckeys.tpl.html',
            }).when('/searchkeys', {
                title: "Search Keys",
                templateUrl: 'app/search/searchkeys.tpl.html',
            }).when('/users', {
                title: "Users",
                templateUrl: 'app/users/users.html',
            }).otherwise({
                redirectTo: '/errors'
            });
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
        self.$get = ['$window',
            function($window) {
                var globals = {
                    'dateFormat': {
                        'service': 'yyyy-MM-ddTHH:mm:ss',
                        'datepicker': 'yyyy/MM/dd',
                        'timepicker': 'HH:mm:ss'
                    },
                    'serverSideLogging': {
                        'info': false,
                        'debug': false,
                        'warn': true,
                        'error': true
                    }
                };
                return globals;
            }
        ];
    }
);

esbMessageAdminApp.factory('samlResponseInterceptor', ['$window', '$timeout',
    function($window, $timeout) {
        function jqElementContainsSamlRequest(jqElement) {
            for (var i = 0; i < jqElement.length; i++) {
                var e = jqElement[i];
                if (e.tagName === 'FORM' &&
                    e.querySelector('input[name="SAMLRequest"]') !== null) {
                    return true;
                }
            }
            return false;
        }

        return {
            response: function(response) {
                var contentType = response.headers('Content-Type');

                if (contentType !== null && contentType.indexOf("text/html") !== -1) {
                    var responseAsJqElement = angular.element(response.data);

                    if (jqElementContainsSamlRequest(responseAsJqElement)) {
                        // Schedule reload on next tick to let xhr request finish normally.
                        $timeout(function() {
                            $window.location.reload();
                        });
                    }
                }

                return response;
            }
        };
    }
]);

esbMessageAdminApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('samlResponseInterceptor');
}]);

String.prototype.supplant = function(o) {
    return this.replace(/{([^{}]*)}/g,
        function(a, b) {
            var r = o[b];
            return typeof r === 'string' || typeof r === 'number' ? r : a;
        }
    );
};

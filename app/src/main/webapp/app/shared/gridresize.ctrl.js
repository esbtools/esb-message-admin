esbMessageAdminApp.controller('gridResizeController', [
    '$scope',
    '$rootScope',
    '$element',
    function($scope, $rootScope, $element) {
        $scope.$watch(
            function() {
                return $element.width();
            },
            function() {
                $rootScope.$broadcast('errorGridResize');
            }
        );
    }
]);

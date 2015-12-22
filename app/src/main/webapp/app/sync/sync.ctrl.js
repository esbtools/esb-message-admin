esbMessageAdminApp.controller('SyncCtrl',
	[
        '$scope',
        'EsbMessageService',
        function($scope, EsbMessageService) {
            $scope.entities = {
                "id" : 0,
                "name" : "Entities",
                "type" : "Entities",
                "value" : "entities",
                "children" : []
            };

            EsbMessageService.getSyncKeysTree().then(
            	function(response) {
            		$scope.entities = response.data.tree || $scope.entities;
            	}
            );

            $scope.keys = [];
            $scope.systems = [];

            $scope.syncEntity = "";
            $scope.syncSystem = "";
            $scope.syncKey = "";
            $scope.syncValues = [];

            $scope.enableSubmit = function() {
                if ($scope.syncEntity == "" ||
                	$scope.syncKey == "" ||
                	$scope.syncSystem == "" ||
                	$scope.syncValues.length <= 0) {
                	return true;
                }
            };

            $scope.sync = function() {
                var values = '';

                for ( var value in $scope.syncValues) {
                    values += $scope.syncValues[value] + ",";
                }

                EsbMessageService.sync(
                	$scope.syncEntity.value,
                    $scope.syncSystem.value,
                    $scope.syncKey.value,
                    values
                );
            };

            $scope.entityChange = function() {
                $scope.systems = $scope.syncEntity.children;
                $scope.keys = [];
            };

            $scope.systemChange = function() {
                $scope.keys = $scope.syncSystem.children;
            };
        }
    ]
);

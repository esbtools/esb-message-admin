esbMessageAdminApp.controller('SearchKeysCtrl', [
    '$scope',
    'EsbMessageService',
    function($scope, EsbMessageService) {

        $scope.searchKeys = {
            "id": 0,
            "name": "Search Keys",
            "type": "SearchKeys",
            "value": "searchKeys",
            "children": []
        };

        $scope.getChildTypes = function(type) {
            if (type === "SearchKeys") {
                return ["SearchKey"];
            } else {
                return ["XPATH", "Suggestion"];
            }
        };

        $scope.getPeerTypes = function(type) {
            if (type === "SearchKey") {
                return ["SearchKey"];
            } else {
                return ["XPATH", "Suggestion"];
            }
        };

        $scope.updateParent = function(parent) {
            $scope.keyType = null;
            $scope.parent = parent;
            $scope.childTypes = $scope.getChildTypes($scope.parent.type);
            $scope.childKeyType = $scope.childTypes[0];
            $scope.peerTypes = $scope.getPeerTypes($scope.parent.type);
            $scope.peerKeyType = $scope.peerTypes[0];
        };

        $scope.updateParent($scope.searchKeys);

        $scope.crumbs = [$scope.searchKeys];
        $scope.addMode = false;
        $scope.updateMode = false;

        EsbMessageService.getSearchKeysTree().then(
            function(response) {
                $scope.searchKeys = response.data.tree || $scope.searchKeys;
                $scope.updateParent($scope.searchKeys || $scope.parent);
                $scope.crumbs = [$scope.searchKeys];
            }
        );

        $scope.addChild = function(parent) {
            $scope.addMode = true;
            $scope.updateParent(parent);
            var newCrumb = {
                "value": "Add new child"
            };
            $scope.crumbs.push(newCrumb);
        };

        $scope.requestAdd = function() {
            var name = $scope.parent.type;
            EsbMessageService.addKey(
                    $scope.parent.id, name,
                    $scope.childKeyType,
                    $scope.addFormValue)
                .then(
                    function(response) {
                        $scope.searchKeys = response.data.tree || $scope.searchKeys;
                        $scope.updateParent(response.data.result || $scope.parent);
                    }
                );
            $scope.addFormValue = "";
            $scope.addMode = false;
            $scope.crumbs.pop();
        };

        $scope.editChild = function(field) {
            $scope.updateMode = true;
            $scope.updateParent(field);
            $scope.crumbs.push(field);
        };

        $scope.requestUpdate = function() {
            EsbMessageService.updateKey(
                    $scope.parent.id,
                    $scope.peerKeyType,
                    $scope.peerKeyType,
                    $scope.parent.value)
                .then(
                    function(response) {
                        $scope.searchKeys = response.data.tree || $scope.searchKeys;
                        $scope.updateParent(response.data.result || $scope.parent);
                    }
                );
            $scope.crumbs.pop();
            $scope.updateMode = false;
        };

        $scope.deleteChild = function(field) {
            EsbMessageService.deleteKey(field.id).then(
                function(response) {
                    $scope.searchKeys = response.data.tree || $scope.searchKeys;
                    $scope.updateParent(response.data.result || $scope.parent);
                }
            );
        };

        $scope.manageChildren = function(field) {
            $scope.updateParent(field);
            $scope.crumbs.push(field);
        };

        $scope.cantHaveChild = function(field) {

            if (field == null ||
                field.type == null ||
                field.type === "Suggestion" ||
                field.type === "XPATH") {
                return true;
            }
        };

        $scope.gotoCrumb = function(crumb) {

            $scope.addMode = false;
            $scope.updateMode = false;

            if (crumb.type === "SearchKeys") {
                $scope.updateParent($scope.searchKeys);
            } else {
                var currChildren = $scope.searchKeys.children;
                for (i = 1; i < $scope.crumbs.length; i++) {
                    for (j = 0; j < currChildren.length; j++) {
                        if (crumb.id == currChildren[j].id) {
                            $scope.updateParent(currChildren[j]);
                            // exit all loops
                            j = currChildren.length + 1;
                            i = $scope.crumbs.length + 1;
                        } else if ($scope.crumbs[i].id === currChildren[j].id) {
                            currChildren = currChildren[j].children;
                            // exit inner loop
                            j = currChildren.length + 1;
                        }
                    }
                }
            }

            if (crumb.type === "SearchKeys") {
                $scope.crumbs = $scope.crumbs.slice(0, 1);
            } else if (crumb.type == "SearchKey") {
                $scope.crumbs = $scope.crumbs.slice(0, 2);
            }
        };
    }
]);

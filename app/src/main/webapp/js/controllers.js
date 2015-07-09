var esbMessageAdminControllers = angular.module('esbMessageAdminControllers',
        []);

esbMessageAdminControllers
        .controller(
                'ErrorsSearchCtrl',
                [
                        '$scope',
                        '$rootScope',
                        'EsbMessageService',
                        '$log',
                        'Globals',
                        function($scope, $rootScope, EsbMessageService, $log,
                                Globals) {

                            // initialize autocomplete data
                            EsbMessageService
                                    .getSuggestions()
                                    .then(
                                            function(response) {
                                                $scope.autocompleteData = response.data;
                                            }, function(error) {
                                                // TODO: handle error
                                                $log.error(error.status);
                                            });

                            $scope.messageDetails = {
                                errorMessage : "",
                                errorDetails : "",
                                payload : "",
                            };

                            $scope.messageSelections = [];

                            // ngGrid

                            var standardCellTemplate = '<div class="ngCellText" ng-class="col.colIndex()" title="{{row.getProperty(col.field)}}">{{row.getProperty(col.field)}}</div>';
                            var dateCellTempate = '<div class="ngCellText" ng-class="col.colIndex()">{{row.getProperty(col.field) | date:"M/d/yy HH:mm:ss"}}</div>';

                            var columnDefs = [ {
                                field : 'sourceSystem',
                                displayName : 'Source',
                                width : 110,
                                cellTemplate : standardCellTemplate
                            }, {
                                field : 'messageType',
                                displayName : 'Type',
                                width : 110,
                                cellTemplate : standardCellTemplate
                            }, {
                                field : 'timestamp',
                                displayName : 'Timestamp',
                                width : 130,
                                cellTemplate : dateCellTempate
                            }, {
                                field : 'occurrenceCount',
                                displayName : '#',
                                width : 10,
                                cellTemplate : standardCellTemplate
                            } ];

                            $scope.sortOptions = {
                                fields : [ "timestamp" ],
                                directions : [ "asc" ]
                            };

                            $scope.totalServerItems = 0;

                            $scope.pagingOptions = {
                                pageSizes : [ 12, 20, 50, 100 ],
                                pageSize : 12,
                                currentPage : 1
                            };

                            $scope.gridOptions = {
                                // plugins: [layoutPlugin],
                                data : 'messages',
                                columnDefs : columnDefs,
                                enablePaging : true,
                                showFooter : true,
                                totalServerItems : 'totalServerItems',
                                pagingOptions : $scope.pagingOptions,
                                selectedItems : $scope.messageSelections,
                                multiSelect : false,
                                useExternalSorting : true,
                                sortInfo : $scope.sortOptions,
                            };

                            var parseSearchString = function(searchStr) {
                                var termsArray = searchStr.split(';');
                                var criteria = {};
                                termsArray
                                        .forEach(function(term) {
                                            var keyValue = term.split('=');
                                            if (keyValue.length == 2) {
                                                criteria[keyValue[0].trim()] = keyValue[1]
                                                        .replace(/"/g, '')
                                                        .trim();
                                            } else {
                                                if (term)
                                                    $log
                                                            .error("Could not parse: "
                                                                    + term);
                                            }
                                        });
                                return criteria;
                            };

                            var prepareResultsPage = function() {
                                var first = ($scope.pagingOptions.currentPage - 1)
                                        * $scope.pagingOptions.pageSize;
                                var maxResults = $scope.pagingOptions.pageSize;

                                var sortField = $scope.sortOptions.fields[0];
                                var sortAsc = ($scope.sortOptions.directions[0] === "asc");

                                if ($rootScope.searchField_searchStr) {
                                    var criteria = parseSearchString($rootScope.searchField_searchStr);

                                    EsbMessageService
                                            .search(criteria, $scope.fromDate,
                                                    $scope.toDate, first,
                                                    maxResults, sortField,
                                                    sortAsc)
                                            .then(
                                                    function(response) {
                                                        $scope.messages = response.data.messages;
                                                        $scope.totalServerItems = response.data.totalResults;

                                                        if ($scope.totalServerItems === 0) {
                                                            // TODO: show
                                                            // message
                                                            alert("Query returned no results");
                                                        }

                                                        if (!$scope.$$phase) {
                                                            $scope.$apply();
                                                        }

                                                        // TODO: error display
                                                    });
                                }
                            };

                            $scope.search = function() {
                                prepareResultsPage();
                            };

                            $scope.$watch('pagingOptions', function(newVal,
                                    oldVal) {
                                if (newVal !== oldVal) {
                                    prepareResultsPage();
                                }
                            }, true);

                            $scope.$watch('sortOptions', function(newVal,
                                    oldVal) {
                                if (newVal !== oldVal) {
                                    prepareResultsPage();
                                }
                            }, true);

                            $scope
                                    .$watch(
                                            'messageSelections',
                                            function() {

                                                if ($scope.messageSelections.length > 0)
                                                    $rootScope.selectedMessage = $scope.messageSelections[0];
                                                else
                                                    delete $rootScope.selectedMessage;

                                                // $scope.gridOptions.selectedItems.forEach(function(entry)
                                                // {

                                                // //TODO: call service to get
                                                // details
                                                // $scope.messageDetails.errorMessage
                                                // = "The error message";
                                                // $scope.messageDetails.errorDetails
                                                // = "The Stacktrace";
                                                // $scope.messageDetails.payload
                                                // = "The payload";
                                                // });
                                            }, true);

                            // Date picker stuff below
                            $scope.dateFormat = Globals.dateFormat.datepicker;
                            $scope.timeFormat = Globals.dateFormat.timepicker;

                            $scope.maxDate = new Date(); // now

                            $scope.toDate = new Date(2014, 10, 20); // TODO:
                            // change
                            // this now.
                            // Using
                            // 11/20
                            // because
                            // this is
                            // when test
                            // data
                            // ends.

                            $scope.fromDate = new Date($scope.toDate.getTime());
                            $scope.fromDate
                                    .setDate($scope.fromDate.getDate() - 1); // yesterday

                            $scope.calendarFromOpen = function($event) {
                                $event.preventDefault();
                                $event.stopPropagation();

                                $scope.calendarFromOpened = true;
                            };

                            $scope.calendarToOpen = function($event) {
                                $event.preventDefault();
                                $event.stopPropagation();

                                $scope.calendarToOpened = true;
                            };
                            // Date picker stuff above

                        } ]);

esbMessageAdminControllers.controller('ErrorDetailsCtrl', [
        '$scope',
        '$rootScope',
        'EsbMessageService',
        function($scope, $rootScope, EsbMessageService) {

            // on message select, fetch message details
            $rootScope.$watch('selectedMessage', function() {
                if ($scope.selectedMessage) {
                    EsbMessageService.getMessage($scope.selectedMessage.id)
                            .then(function(result) {
                                $scope.message = result.data.messages[0];
                            });
                }
            });

            $scope.resubmitMessage = function() {
                alert("Not implemented yet");
            };

        } ]);

esbMessageAdminControllers
        .controller(
                'SearchKeysCtrl',
                [
                        '$scope',
                        'EsbMessageService',
                        function($scope, EsbMessageService) {

                            $scope.searchKeys = {
                                "id" : 0,
                                "name" : "Search Keys",
                                "type" : "SearchKeys",
                                "value" : "searchKeys",
                                "children" : []
                            };

                            $scope.getChildTypes = function(type) {
                                if (type === "SearchKeys") {
                                    return [ "SearchKey" ];
                                } else {
                                    return [ "XPATH", "Suggestion" ];
                                }
                            };

                            $scope.getPeerTypes = function(type) {
                                if (type === "SearchKey") {
                                    return [ "SearchKey" ];
                                } else {
                                    return [ "XPATH", "Suggestion" ];
                                }
                            };

                            $scope.updateParent = function(parent) {
                                $scope.keyType = null;
                                $scope.parent = parent;
                                $scope.childTypes = $scope
                                        .getChildTypes($scope.parent.type);
                                $scope.childKeyType = $scope.childTypes[0];
                                $scope.peerTypes = $scope
                                        .getPeerTypes($scope.parent.type);
                                $scope.peerKeyType = $scope.peerTypes[0];
                            };

                            $scope.updateParent($scope.searchKeys);

                            $scope.crumbs = [ $scope.searchKeys ];
                            $scope.addMode = false;
                            $scope.updateMode = false;

                            EsbMessageService.getSearchKeysTree().then(
                                    function(response) {
                                        $scope.searchKeys = response.data.tree
                                                || $scope.searchKeys;
                                        $scope.updateParent($scope.searchKeys
                                                || $scope.parent);
                                        $scope.crumbs = [ $scope.searchKeys ];
                                    });

                            $scope.addChild = function(parent) {
                                $scope.addMode = true;
                                $scope.updateParent(parent);
                                var newCrumb = {
                                    "value" : "Add new child"
                                };
                                $scope.crumbs.push(newCrumb);
                            };

                            $scope.requestAdd = function() {

                                var name = $scope.parent.type;
                                EsbMessageService
                                        .addKey($scope.parent.id, name,
                                                $scope.childKeyType,
                                                $scope.addFormValue)
                                        .then(
                                                function(response) {
                                                    $scope.searchKeys = response.data.tree
                                                            || $scope.searchKeys;
                                                    $scope
                                                            .updateParent(response.data.result
                                                                    || $scope.parent);
                                                });
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

                                EsbMessageService
                                        .updateKey($scope.parent.id,
                                                $scope.peerKeyType,
                                                $scope.peerKeyType,
                                                $scope.parent.value)
                                        .then(
                                                function(response) {
                                                    $scope.searchKeys = response.data.tree
                                                            || $scope.searchKeys;
                                                    $scope
                                                            .updateParent(response.data.result
                                                                    || $scope.parent);
                                                });
                                $scope.crumbs.pop();
                                $scope.updateMode = false;
                            };

                            $scope.deleteChild = function(field) {
                                EsbMessageService
                                        .deleteKey(field.id)
                                        .then(
                                                function(response) {
                                                    $scope.searchKeys = response.data.tree
                                                            || $scope.searchKeys;
                                                    $scope
                                                            .updateParent(response.data.result
                                                                    || $scope.parent);
                                                });
                            };

                            $scope.manageChildren = function(field) {
                                $scope.updateParent(field);
                                $scope.crumbs.push(field);
                            };

                            $scope.cantHaveChild = function(field) {

                                if (field == null || field.type == null
                                        || field.type === "Suggestion"
                                        || field.type === "XPATH")
                                    return true;
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
                                                $scope
                                                        .updateParent(currChildren[j]);
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

                        } ]);

esbMessageAdminControllers
        .controller(
                'SyncKeysCtrl',
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
                                        $scope.parent = response.data.tree
                                                || $scope.parent;
                                        $scope.entities = response.data.tree
                                                || $scope.entities;
                                        $scope.crumbs = [ $scope.entities ];
                                    });

                            $scope.crumbs = [ $scope.entities ];

                            $scope.parent = $scope.entities;

                            $scope.addMode = false;
                            $scope.updateMode = false;

                            $scope.cantHaveChild = function(field) {
                                if (field.type === "SyncKey")
                                    return true;
                            };

                            $scope.addChild = function(parent) {
                                $scope.addMode = true;
                                $scope.parent = parent;
                                var newCrumb = {
                                    "name" : "Add new "
                                            + $scope.getNextType(parent.type)
                                };
                                $scope.crumbs.push(newCrumb);
                            };

                            $scope.requestAdd = function() {

                                var type;
                                switch ($scope.parent.type) {
                                case "Entities":
                                    type = "Entity";
                                    break;
                                case "Entity":
                                    type = "System";
                                    break;
                                case "System":
                                    type = "SyncKey";
                                    break;

                                }
                                EsbMessageService
                                        .addKey($scope.parent.id,
                                                $scope.addFormName, type,
                                                $scope.addFormValue)
                                        .then(
                                                function(response) {
                                                    $scope.entities = response.data.tree
                                                            || $scope.entities;
                                                    $scope.parent = response.data.result
                                                            || $scope.parent;
                                                });
                                $scope.addFormName = "";
                                $scope.addFormValue = "";
                                $scope.addMode = false;
                                $scope.crumbs.pop();

                            };

                            $scope.editChild = function(field) {
                                $scope.updateMode = true;
                                $scope.parent = field;
                                $scope.crumbs.push(field);
                            };

                            $scope.requestUpdate = function() {
                                EsbMessageService
                                        .updateKey($scope.parent.id,
                                                $scope.parent.name,
                                                $scope.parent.type,
                                                $scope.parent.value)
                                        .then(
                                                function(response) {
                                                    $scope.entities = response.data.tree
                                                            || $scope.entities;
                                                    $scope.parent = response.data.result
                                                            || $scope.parent;
                                                });
                                $scope.crumbs.pop();
                                $scope.updateMode = false;
                            };

                            $scope.deleteChild = function(field) {
                                EsbMessageService
                                        .deleteKey(field.id)
                                        .then(
                                                function(response) {
                                                    $scope.entities = response.data.tree
                                                            || $scope.entities;
                                                    $scope.parent = response.data.result
                                                            || $scope.parent;
                                                });
                            };

                            $scope.gotoCrumb = function(crumb) {

                                $scope.addMode = false;
                                $scope.updateMode = false;

                                if (crumb.type === "Entities") {
                                    $scope.parent = $scope.entities;
                                } else {
                                    var currChildren = $scope.entities.children;
                                    for (i = 1; i < $scope.crumbs.length; i++) {
                                        for (j = 0; j < currChildren.length; j++) {
                                            if (crumb.id == currChildren[j].id) {
                                                $scope.parent = currChildren[j];
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
                                if (crumb.type === "Entities") {
                                    $scope.crumbs = $scope.crumbs.slice(0, 1);
                                } else if (crumb.type === "Entity") {
                                    $scope.crumbs = $scope.crumbs.slice(0, 2);
                                } else if (crumb.type === "System") {
                                    $scope.crumbs = $scope.crumbs.slice(0, 3);
                                }
                            };

                            $scope.manageChildren = function(field) {
                                $scope.parent = field;
                                $scope.crumbs.push(field);
                            };

                            $scope.getNextType = function(currType, grandChild) {
                                if (currType === "Entities") {
                                    if (grandChild) {
                                        return "systems";
                                    } else {
                                        return "entity";
                                    }
                                } else if (currType === "Entity") {
                                    if (grandChild) {
                                        return "keys";
                                    } else {
                                        return "system";
                                    }
                                } else if (currType === "System") {
                                    if (!grandChild) {
                                        return "key";
                                    }
                                }
                            };

                        } ]);

esbMessageAdminControllers.controller('SyncCtrl', [
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

            EsbMessageService.getSyncKeysTree().then(function(response) {
                $scope.entities = response.data.tree || $scope.entities;
            });

            $scope.keys = [];
            $scope.systems = [];

            $scope.syncEntity = "";
            $scope.syncSystem = "";
            $scope.syncKey = "";
            $scope.syncValues = [];

            $scope.enableSubmit = function() {
                if ($scope.syncEntity == "" || $scope.syncKey == ""
                        || $scope.syncSystem == ""
                        || $scope.syncValues.length <= 0)
                    return true;
            };

            $scope.sync = function() {

                var values = '';
                for ( var value in $scope.syncValues) {
                    values += $scope.syncValues[value] + ",";
                }
                EsbMessageService.sync($scope.syncEntity.value,
                        $scope.syncSystem.value, $scope.syncKey.value, values);
            };

            $scope.entityChange = function() {
                $scope.systems = $scope.syncEntity.children;
                $scope.keys = [];
            };

            $scope.systemChange = function() {
                $scope.keys = $scope.syncSystem.children;
            };
        } ]);

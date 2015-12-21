esbMessageAdminApp.controller('ErrorsSearchCtrl',
    [
		'$scope',
		'$rootScope',
		'EsbMessageService',
		'errorColumnPrefs',
		'$log',
		'Globals',
		'ngGridLayoutPlugin',
		function($scope, $rootScope, EsbMessageService, errorColumnPrefs, $log, Globals, layoutPlugin) {

		    // initialize autocomplete data
		    EsbMessageService.getSuggestions().then(
		        function(response) {
		            $scope.autocompleteData = response.data;
		        }, function(error) {
		            // TODO: handle error
		            $log.error(error.status);
		        }
		    );
		    $scope.message = null;
		    $scope.messageSelections = [];

		    // ngGrid
		    $scope.$on(
		        'ngGridEventColumns',
		        function(event, columns) {
		            errorColumnPrefs.save(columns);
		        }
		    );

		    var columnDefs = errorColumnPrefs.load();

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

		    $scope.$on(
		    	'errorGridResize',
		    	function() {
		    		layoutPlugin.updateGridLayout();
		    	}
		    );

		    $scope.gridOptions = {
		        plugins: [layoutPlugin],
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
		        showColumnMenu: true,
		        enableColumnReordering: true,
		    };

		    var parseSearchString = function(searchStr) {
		        var searchParams = searchStr.split(';');
		        var criteria = {};
		        searchParams.forEach(function(param) {
		        	var keyValue = param.split('=');
		            if (keyValue.length == 2) {
		            	criteria[keyValue[0].trim()] = keyValue[1]
		                .replace(/"/g, '')
		                .trim();
		            } else {
		                if (param) {
		                	$log.error("Could not parse: "+ param);
		                }
		            }
		          }
		        );
		        return criteria;
		    };

		    var prepareResultsPage = function() {
		        var first = ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize;
		        var maxResults = $scope.pagingOptions.pageSize;
		        var sortField = $scope.sortOptions.fields[0];
		        var sortAsc = ($scope.sortOptions.directions[0] === "asc");

		        if ($rootScope.searchField_searchStr) {
		            var criteria = parseSearchString($rootScope.searchField_searchStr);

		            EsbMessageService.search(
		                criteria,
		                $scope.fromDate,
		                $scope.toDate,
		                first,
		                maxResults,
		                sortField,
		                sortAsc
		            ).then(
		                function(response) {
		                	$scope.messages = response.data.messages;
		                	$scope.totalServerItems = response.data.totalResults;
		                  $scope.messageSelections.splice(0, $scope.messageSelections.length);
		                }
		            );
		        }
		    };

		    $scope.search = function() {
		        prepareResultsPage();
		    };

		    $scope.$watch('pagingOptions',
		    	function(newVal, oldVal) {
		    		if (newVal !== oldVal) {
		    			prepareResultsPage();
		    		}
		    	},
		    	true
		    );

		    $scope.$watch('sortOptions',
		    	function(newVal,oldVal) {
		        	if (newVal !== oldVal) {
		        		prepareResultsPage();
		        	}
		    	},
		    	true
		    );

		    $scope.$watch('messageSelections',
		        function() {
		            if ($scope.messageSelections.length > 0) {
		            	$rootScope.selectedMessage = $scope.messageSelections[0];
		            } else {
		            	delete $rootScope.selectedMessage;
		            }
		        },
		        true
		    );

		    // Begin Date picker stuff
		    $scope.dateFormat = Globals.dateFormat.datepicker;
		    $scope.timeFormat = Globals.dateFormat.timepicker;

		    $scope.maxDate = new Date(); // now

		    $scope.toDate = new Date(); // now

		    $scope.fromDate = new Date($scope.toDate.getTime());
		    $scope.fromDate.setDate($scope.fromDate.getDate() - 1); // yesterday

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
		    // End Date picker stuff
		}
	]
);

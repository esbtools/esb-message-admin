esbMessageAdminApp.controller('ErrorCtrl', [
  '$scope',
  '$rootScope',
  'EsbMessageService',
  'errorColumnPrefs',
  '$log',
  'Globals',
  'ngGridLayoutPlugin',
  'messageCenterService',
  function($scope, $rootScope, EsbMessageService, errorColumnPrefs, $log, Globals, layoutPlugin, messageCenterService) {
    $scope.message = null;
    $scope.messageSelections = [];

    $scope.availableFilters = null;
    $scope.availableSystems = null;
    $scope.availableEntities = null;

    // Fetch message search configurations
    EsbMessageService.getConfigs().then(function(result) {
      window.resAvailableFilter = result.data.searchFilters;
      $scope.availableFilters = result.data.searchFilters;
      $scope.availableSystems = result.data.searchSystems;
      $scope.availableEntities = result.data.searchEntities;
    });

    $scope.messageFilters = [
      { "key" : "", "value" : ""}
    ];

    $scope.searchCriteria = {
      "messageType"  : null,
      "sourceSystem" : null,
      "filters" : $scope.messageFilters
    };

    // ngGrid
    $scope.$on(
      'ngGridEventColumns',
      function(event, columns) {
        errorColumnPrefs.save(columns);
      }
    );

    var columnDefs = errorColumnPrefs.load();

    $scope.sortOptions = {
      fields: ["timestamp"],
      directions: ["asc"]
    };

    $scope.totalServerItems = 0;

    $scope.pagingOptions = {
      pageSizes: [12, 20, 50, 100],
      pageSize: 12,
      currentPage: 1
    };

    $scope.$on(
      'errorGridResize',
      function() {
        layoutPlugin.updateGridLayout();
      }
    );

    $scope.gridOptions = {
      plugins: [layoutPlugin],
      data: 'messages',
      columnDefs: columnDefs,
      enablePaging: true,
      showFooter: true,
      totalServerItems: 'totalServerItems',
      pagingOptions: $scope.pagingOptions,
      selectedItems: $scope.messageSelections,
      multiSelect: false,
      useExternalSorting: true,
      sortInfo: $scope.sortOptions,
      showColumnMenu: true,
      enableColumnReordering: true,
    };

    $scope.addFilter = function() {
      $scope.messageFilters.push({"key" : "", "value" : ""});
    };

    $scope.removeFilter = function(index) {
      $scope.messageFilters.splice(index, 1);
    };

    var parseSearchCriteria = function(searchCriteria) {
      var criteria = {};
      criteria.messageType = searchCriteria.messageType;
      criteria.sourceSystem = searchCriteria.sourceSystem;

      for (var i = 0; i < searchCriteria.filters.length; i++) {
        criteria[searchCriteria.filters[i].key] = searchCriteria.filters[i].value;
      }

      return criteria;
    };

    var prepareResultsPage = function() {
      var first = ($scope.pagingOptions.currentPage - 1) * $scope.pagingOptions.pageSize;
      var maxResults = $scope.pagingOptions.pageSize;
      var sortField = $scope.sortOptions.fields[0];
      var sortAsc = ($scope.sortOptions.directions[0] === "asc");
      var emptySearch = (!$scope.searchCriteria.sourceSystem && !$scope.searchCriteria.messageType);

      if (!emptySearch) {
        var criteria = parseSearchCriteria($scope.searchCriteria);

        EsbMessageService.search(
          criteria,
          $scope.fromDate,
          $scope.toDate,
          first,
          maxResults,
          sortField,
          sortAsc
        ).then(
          function (response) {
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
      function(newVal, oldVal) {
        if (newVal !== oldVal) {
          prepareResultsPage();
        }
      },
      true
    );

    $scope.$watch('messageSelections',
      function() {
        if ($scope.messageSelections.length > 0) {
          EsbMessageService.getMessage($scope.messageSelections[0].id).then(
            function(result) {
              $scope.message = result.data.messages[0];
              if (result.data.messages[0].payload) {
                var payload = result.data.messages[0].payload;
                var formatted = '';
                var reg = /(>)\s*(<)(\/*)/g;
                payload = payload.replace(reg, '$1\r\n$2$3');
                var pad = 0;
                jQuery.each(payload.split('\r\n'), function(index, node) {
                  var indent = 0;
                  if (node.match(/.+<\/\w[^>]*>$/)) {
                    indent = 0;
                  } else if (node.match(/^<\/\w/)) {
                    if (pad != 0) {
                      pad -= 1;
                    }
                  } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
                    indent = 1;
                  } else {
                    indent = 0;
                  }

                  var padding = '';
                  for (var i = 0; i < pad; i++) {
                    padding += '  ';
                  }

                  formatted += padding + node + '\r\n';
                  pad += indent;
                });
                $scope.message.payload = formatted;
              }
            }
          );
        } else {
          $scope.message = null;
        }
      },
      true
    );

    // Begin Date picker stuff
    $scope.dateFormat = Globals.dateFormat.datepicker;
    $scope.timeFormat = Globals.dateFormat.timepicker;

    $scope.maxDate = new Date(); // now

    $scope.toDate = new Date();

    $scope.fromDate = new Date($scope.toDate.getTime());
    $scope.fromDate.setDate($scope.fromDate.getDate() - 1); // yesterday
    $scope.fromDate.setHours(0, 0, 0, 0);

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

    $scope.resubmitMessage = function() {
      // TODO: (bmiller) Here, I removed the check for resubmittable messages.
      // Might need to add it back, or remove the rest of the functionality
      var message = $scope.message;

      EsbMessageService.resubmitMessage(message).then(
        function(response) {
          if (response.data.status === "Success") {
            messageCenterService.add('success', 'Resubmit successful!', {
              timeout: 3000
            });
          } else {
            messageCenterService.add('danger', response.data.errorMessage, {
              status: messageCenterService.status.permanent
            });
          }
        },
        function(err) {
          var errorMessage = "There was a problem communicating with the server. Server Returned: "
            + err.status + ": " + err.data;
          messageCenterService.add('danger', errorMessage, {
            status: messageCenterService.status.permanent
          });
        });
    };
  }
]);

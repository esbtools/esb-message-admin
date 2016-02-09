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

    // initialize autocomplete data
    EsbMessageService.getSuggestions().then(
      function(response) {
        $scope.autocompleteData = response.data;
      },
      function(error) {
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
            $log.error("Could not parse: " + param);
          }
        }
      });
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
                var reg = /(>)\s(<)(\/*)/g;
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

    $scope.resubmitMessage = function() {
      var message = $scope.message;

      if (!message.allowsResubmit) {
        messageCenterService.add('warning', 'Message can not be resubmitted', {
          timeout: 5000
        });

      } else {
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
      }
    };
  }
]);

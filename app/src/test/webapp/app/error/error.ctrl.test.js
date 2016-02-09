describe("ErrorCtrl", function() {
  var $controller, $scope, $rootScope, $q, messageCenterService, msgSvc;

  var emptySearchResponse = {
    data: {
      messages: [],
      totalResults: 0
    }
  };

  var testMessage = {
    id: "testMessage"
  };

  var resubmittableMessage = {
    id: "testMessage",
    allowsResubmit: true
  };

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(module(function($provide) {
    $provide.service("EsbMessageService", function() {
      // Overwrite properties if different behavior is needed per test
      // Service is recreated before each so overwrites will not affect other
      // tests.
      return {
        // Never resolves to anything.
        getSuggestions: function() {
          var deferred = $q.defer();
          return deferred.promise;
        },
        // Returns 0 results for any parameters
        search: function() {
          return $q.when(emptySearchResponse);
        },
        // Always returns the same example message
        getMessage: function() {
          return $q.when({
            data: {
              messages: [testMessage]
            }
          });
        },

        resubmitMessage: function() {
          return $q.when({
            data: {
              status: "Success"
            }
          });
        }
      };
    });

    $provide.service("errorColumnPrefs", function() {
      var _columns = [];

      return {
        save: function(columns) {
          _columns = columns;
        },
        load: function() {
          return _columns;
        }
      };
    });

  }));

  beforeEach(inject(function(_$controller_, _$rootScope_, _$q_, _messageCenterService_, _EsbMessageService_) {
    $controller = _$controller_;
    $rootScope = _$rootScope_;
    $q = _$q_;
    messageCenterService = _messageCenterService_;
    msgSvc = _EsbMessageService_;
    $scope = $rootScope.$new();
  }));

  it("updates grid layout on errorGridResize event", function() {
    var ngGridLayoutPlugin;

    inject(function(_ngGridLayoutPlugin_) {
      ngGridLayoutPlugin = _ngGridLayoutPlugin_;
    });

    spyOn(ngGridLayoutPlugin, 'updateGridLayout');

    $controller("ErrorCtrl", {
      $scope: $scope
    });

    $scope.$emit("errorGridResize");
    $scope.$apply();

    expect(ngGridLayoutPlugin.updateGridLayout).toHaveBeenCalled();
  });

  it("loads stored column prefs into $scope.gridOptions.columnDefs", function() {
    inject(function(errorColumnPrefs) {
      errorColumnPrefs.load = function() {
        return ["foo", "bar"];
      };
    });

    $controller("ErrorCtrl", {
      $scope: $scope
    });

    expect($scope.gridOptions.columnDefs).toEqual(["foo", "bar"]);
  });

  it("saves error grid column state on ngGridEventColumns event", function() {
    var savedColumns;

    inject(function(errorColumnPrefs) {
      errorColumnPrefs.save = function(columns) {
        savedColumns = columns;
      };
    });

    $controller("ErrorCtrl", {
      $scope: $scope
    });

    $scope.$emit("ngGridEventColumns", ["foo", "bar"]);
    $scope.$apply();

    expect(savedColumns).toEqual(["foo", "bar"]);
  });

  describe("with nested ErrorCtrl", function() {
    var $detailsScope;

    beforeEach(function() {
      $detailsScope = $scope.$new();
    });

    describe("after selecting a message from search results", function() {
      beforeEach(function searchAndSelectResult() {
        // Set required search properties on $scope to perform search();
        $scope.pagingOptions = {
          currentPage: 1,
          pageSize: 1,
        };

        $scope.sortOptions = {
          fields: ["foo"],
          directions: ["asc"]
        };

        $rootScope.searchField_searchStr = "foo=bar";

        msgSvc.search = function() {
          // Simulate a result for the first call...
          return $q.when({
            data: {
              messages: [{}],
              totalresults: 1
            }
          });
        };

        // Run injected controller functions on their scopes
        $controller("ErrorCtrl", {
          $scope: $scope
        });

        $scope.search();
        $scope.$apply();
        $scope.messageSelections = [{
          id: 1
        }];
        $scope.$apply();
      });

      it("populates {{message}} in ErrorCtrl with the selection", function() {
        expect($scope.$eval("message")).toEqual(testMessage);
      });

      it("resets {{message}} in ErrorCtrl after a second search " +
        "which returns no results",
        function() {
          // Simulate no results for next search
          msgSvc.search = function() {
            return $q.when(emptySearchResponse);
          };

          $scope.search();
          $scope.$apply();

          expect($detailsScope.$eval("message")).toBeFalsy();
        });
    });
  });

  describe("success after calling resubmit", function() {
    it("should display a success message when the message is resubmitted", function() {
      spyOn(messageCenterService, 'add');

      $controller("ErrorCtrl", {
        $scope: $scope
      });

      $scope.message = resubmittableMessage;

      $scope.resubmitMessage();
      $scope.$apply();

      expect(messageCenterService.add).toHaveBeenCalledWith('success', 'Resubmit successful!', { "timeout": 3000 });
    });
  });

  describe("failure after calling resubmit", function() {
    it("should display a failure message when a message isn't resubmittable", function(){

      var messageWithoutResubmitHeader = {
        id: 1
      };

      spyOn(messageCenterService, 'add');

      $controller("ErrorCtrl", {
        $scope: $scope
      });

      $scope.message = messageWithoutResubmitHeader;

      $scope.resubmitMessage();

      expect(messageCenterService.add).toHaveBeenCalledWith('warning', 'Message can not be resubmitted', {
        timeout: 5000
      });

    });

    beforeEach(function failedCallToResubmitBackend() {
      msgSvc.resubmitMessage = function() {
        return $q.when({
          data: {
            "status": "Failure",
            "errorMessage": "Just failed"
          }
        });

      };
    });

    it("should display failure message when there's a server problem", function() {
      spyOn(messageCenterService, 'add');

      $controller("ErrorCtrl", {
        $scope: $scope
      });

      $scope.message = resubmittableMessage;

      $scope.resubmitMessage();
      $scope.$apply();:w

      expect(messageCenterService.add).toHaveBeenCalledWith('danger', 'Just failed', { "status": messageCenterService.status.permanent });
    });
  });
});

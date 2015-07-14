describe("ErrorsSearchCtrl", function() {
  var $controller, $scope, $rootScope, $q, msgSvc;

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(module(function($provide) {
    $provide.service("EsbMessageService", function() {
      return {
        // Never resolves to anything.
        // Overwrite property if different behavior is needed.
        getSuggestions: function() {
          var deferred = $q.defer();
          return deferred.promise;
        }
      };
    });

    $provide.service("errorColumnPrefs", function() {
      var _columns = [];

      return {
        save: function(columns) {
          _columns = columns;
        },
        load: function() { return _columns; }
      };
    });
  }));

  beforeEach(inject(function(_$controller_, _$rootScope_, _$q_, _EsbMessageService_) {
    $controller = _$controller_;
    $rootScope = _$rootScope_;
    $q = _$q_;
    msgSvc = _EsbMessageService_;
    $scope = $rootScope.$new();
  }));

  it("updates grid layout on errorGridResize event", function() {
    var ngGridLayoutPlugin;

    inject(function(_ngGridLayoutPlugin_) {
      ngGridLayoutPlugin = _ngGridLayoutPlugin_;
    });

    spyOn(ngGridLayoutPlugin, 'updateGridLayout');

    $controller("ErrorsSearchCtrl", {$scope: $scope});

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

    $controller("ErrorsSearchCtrl", {$scope: $scope});

    expect($scope.gridOptions.columnDefs).toEqual(["foo", "bar"]);
  });

  it("saves error grid column state on ngGridEventColumns event", function() {
    var savedColumns;

    inject(function(errorColumnPrefs) {
      errorColumnPrefs.save = function(columns) {
        savedColumns = columns;
      };
    });

    $controller("ErrorsSearchCtrl", {$scope: $scope});

    $scope.$emit("ngGridEventColumns", ["foo", "bar"]);
    $scope.$apply();

    expect(savedColumns).toEqual(["foo", "bar"]);
  });
});

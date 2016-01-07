describe("GridResizeBroadcastController", function() {
  var $controller, $scope, $rootScope, $element = {};

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(module(function($provide) {
    $provide.service("$element", function() {
      return $element;
    });

    var _width;

    $element.width = function() {
      return _width;
    };
    $element.setWidth = function(width) {
      _width = width;
    };
  }));

  beforeEach(inject(function(_$controller_, _$rootScope_, _$q_, _EsbMessageService_) {
    $controller = _$controller_;
    $rootScope = _$rootScope_;
    $scope = $rootScope.$new();
  }));

  it("broadcasts errorGridSize event to child scopes when $element.width() changes", function(done) {
    $element.setWidth(5);

    $controller("GridResizeBroadcastController", {
      $scope: $rootScope
    });

    // Watch expressions are evaluated on first digest cycle no matter what,
    // so digest first...
    $scope.$apply();

    // ...then set up listener.
    $scope.$on("errorGridResize", function() {
      done();
    });

    $element.setWidth(10);
    $scope.$apply();
  });
});

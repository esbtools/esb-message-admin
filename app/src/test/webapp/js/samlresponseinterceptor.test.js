describe("samlResponseInterceptor", function() {
  var $http, $httpBackend, $timeout, $window;

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(module(function($provide) {
    // Stub window to just toggle wasReloaded() instead of actually reloading.
    $provide.service("$window", function() {
      var wasReloaded = false;
      return {
        location: {
          reload: function() {
            wasReloaded = true;
          }
        },
        wasReloaded: function() {
          return wasReloaded;
        }
      };
    });
  }));

  beforeEach(inject(function(_$http_, _$httpBackend_, _$timeout_, _$window_) {
    $http = _$http_;
    $httpBackend = _$httpBackend_;
    $timeout = _$timeout_;
    $window = _$window_;
  }));

  it("refreshes window in $timeout if response is a SAML HTML redirect page", function() {
    $httpBackend.whenGET('foo').respond(
      '<HTML><HEAD><TITLE>HTTP Post Binding (Request)</TITLE></HEAD><BODY Onload="document.forms[0].submit()"><FORM METHOD="POST" ACTION="https://saml.esbtools.org/"><INPUT TYPE="HIDDEN" NAME="SAMLRequest" VALUE="foobar"/></FORM></BODY></HTML>',
      {
        "Content-Type": "text/html"
      }
    );

    $http.get('foo').then(function() {});
    $httpBackend.flush();

    expect($window.wasReloaded()).toEqual(false);

    $timeout.flush();

    expect($window.wasReloaded()).toEqual(true);
  });

  it("does not refresh window if not a SAML HTML redirect response", function() {
    $httpBackend.whenGET('foo').respond(
      '<html><body>hey look at me being a template and not a saml response</body></html>',
      {
        "Content-Type": "text/html"
      }
    );

    $http.get("foo").then(function() {});
    $httpBackend.flush();
    $timeout.flush();

    expect($window.wasReloaded()).toEqual(false);
  });
});

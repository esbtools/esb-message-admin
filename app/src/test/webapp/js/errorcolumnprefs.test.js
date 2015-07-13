describe("errorColumnPrefs", function() {
  var localStorage = {};
  var errorColumnPrefs;

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(module(function($provide) {
    $provide.service("localStorage", function() {
      return localStorage;
    });

    var stored = {};

    localStorage.setItem = function(key, value) {
      stored[key] = value;
    };

    localStorage.getItem = function(key) {
      return stored[key];
    };
  }));

  beforeEach(inject(function(_errorColumnPrefs_) {
    errorColumnPrefs = _errorColumnPrefs_;
  }));

  it("saves column objects with only 'field' and 'visible' properties " +
      "as JSON strings to localStorage " +
      "with key 'esb_message_admin.error_columns'", function() {
    var sourceSystem = {
      field : 'sourceSystem',
      displayName : 'Source',
      width : '****',
      cellTemplate : "<div>foo</div>",
      visible: true
    };

    var timestamp = {
      field : 'timestamp',
      displayName : 'Timestamp',
      width : '****',
      cellTemplate : "<div>bar</div>",
      visible: false
    };

    var expectedSourceSystem = {
      field: 'sourceSystem',
      visible: true
    };

    var expectedTimestamp = {
      field: 'timestamp',
      visible: false
    };

    errorColumnPrefs.save([sourceSystem, timestamp]);

    var saved = JSON.parse(localStorage.getItem('esb_message_admin.error_columns'));

    expect(saved).toContain(expectedSourceSystem);
    expect(saved).toContain(expectedTimestamp);
    expect(saved.length).toEqual(2);
  });

  it("loads defaults overrided with saved columns' visible' value" +
      "for those columns saved in localStorage", function() {
    var sourceSystem = {
      field : 'sourceSystem',
      displayName : 'Source',
      width : '****',
      cellTemplate : "<div>foo</div>",
      visible: true
    };

    var timestamp = {
      field : 'timestamp',
      displayName : 'Timestamp',
      width : '****',
      cellTemplate : "<div>bar</div>",
      visible: false
    };

    var errorSystem = {
      field : 'errorSystem',
      displayName : 'Error System',
      width : '****',
      cellTemplate : "<div>baz</div>",
    };

    var expectedSourceSystem = angular.copy(sourceSystem);
    var expectedTimestamp = angular.copy(timestamp);
    var expectedErrorSystem = angular.copy(errorSystem);

    expectedSourceSystem.visible = false;
    expectedTimestamp.visible = true;

    // var foo = [{'field': 'sourceSystem', 'visible': false},
    //     {'field': 'timestamp', 'visible': true}];

    localStorage.setItem('esb_message_admin.error_columns',
        '[{"field": "sourceSystem", "visible": false},' +
            '{"field": "timestamp", "visible": true}]');

    errorColumnPrefs.defaults = [sourceSystem, timestamp, errorSystem];
    var loaded = errorColumnPrefs.load();

    expect(loaded).toContain(expectedSourceSystem);
    expect(loaded).toContain(expectedErrorSystem);
    expect(loaded).toContain(expectedTimestamp);
    expect(loaded.size).toEqual(3);
  });

  it("loads defaults unchanged if no column objects are saved in localStorage", function() {

  });
});

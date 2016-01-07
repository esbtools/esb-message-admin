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
    "with key 'esb_message_admin.error_columns'",
    function() {
      var sourceSystem = {
        field: 'sourceSystem',
        displayName: 'Source',
        width: '****',
        cellTemplate: "<div>foo</div>",
        visible: true
      };

      var timestamp = {
        field: 'timestamp',
        displayName: 'Timestamp',
        width: '****',
        cellTemplate: "<div>bar</div>",
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

  it("loads defaults overrided with saved columns' 'visible' value" +
    "for those columns saved in localStorage",
    function() {
      var sourceSystem = {
        field: 'sourceSystem',
        displayName: 'Source',
        width: '****',
        cellTemplate: "<div>foo</div>",
        visible: true
      };

      var timestamp = {
        field: 'timestamp',
        displayName: 'Timestamp',
        width: '****',
        cellTemplate: "<div>bar</div>",
        visible: false
      };

      var errorSystem = {
        field: 'errorSystem',
        displayName: 'Error System',
        width: '****',
        cellTemplate: "<div>baz</div>",
      };

      var updatedSourceSystem = angular.copy(sourceSystem);
      var updatedTimestamp = angular.copy(timestamp);
      var unchangedErrorSystem = angular.copy(errorSystem);

      updatedSourceSystem.visible = false;
      updatedTimestamp.visible = true;

      localStorage.setItem('esb_message_admin.error_columns',
        '[{"field": "sourceSystem", "visible": false},' +
        '{"field": "timestamp", "visible": true}]');

      errorColumnPrefs.default_map = {
        sourceSystem: sourceSystem,
        timestamp: timestamp,
        errorSytem: errorSystem
      };

      var loaded = errorColumnPrefs.load();

      expect(loaded).toContain(updatedSourceSystem);
      expect(loaded).toContain(unchangedErrorSystem);
      expect(loaded).toContain(updatedTimestamp);
      expect(loaded.length).toEqual(3);
    });

  it("loads defaults unchanged if no column objects are saved in localStorage", function() {
    var sourceSystem = {
      field: 'sourceSystem',
      displayName: 'Source',
      width: '****',
      cellTemplate: "<div>foo</div>",
      visible: true
    };

    var timestamp = {
      field: 'timestamp',
      displayName: 'Timestamp',
      width: '****',
      cellTemplate: "<div>bar</div>",
      visible: false
    };

    var errorSystem = {
      field: 'errorSystem',
      displayName: 'Error System',
      width: '****',
      cellTemplate: "<div>baz</div>",
    };

    var unchangedSourceSystem = angular.copy(sourceSystem);
    var unchangedTimestamp = angular.copy(timestamp);
    var unchangedErrorSystem = angular.copy(errorSystem);

    errorColumnPrefs.defaults = [sourceSystem, timestamp, errorSystem];

    var loaded = errorColumnPrefs.load();

    expect(loaded).toContain(unchangedSourceSystem);
    expect(loaded).toContain(unchangedErrorSystem);
    expect(loaded).toContain(unchangedTimestamp);
    expect(loaded.length).toEqual(3);
  });
});

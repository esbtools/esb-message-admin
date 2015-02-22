'use strict';

describe('angucomplete-keyvalue', function() {
  var $compile, $scope, $timeout, $rootScope;

  var element;

  beforeEach(module('angucomplete'));

  beforeEach(inject(function(_$compile_, _$rootScope_, _$timeout_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;
    $scope = $rootScope.$new();
    $timeout = _$timeout_;

    $(document.body).empty();

    element = angular.element('<div angucomplete id="ex1" placeholder="Placeholder text" localdata="autocompleteData"/>');
    element.appendTo(document.body);
    $compile(element)($scope);
    $scope.$digest();
  }));

  var createKeyEvent = function(code) {
    var e = $.Event('keyup');
    e.which = code;
    return e;
  };

  var selectItemInDropdown = function(inputField, _index) {

    var index = 1;
    if (_index)
      index = _index;

    for (var i=0; i<index; i++) {
      var edown = createKeyEvent(40); // down arrow

      inputField.trigger('input');
      inputField.trigger(edown);
    }

    var eenter = createKeyEvent(13); // enter

    inputField.trigger('input');
    inputField.trigger(eenter);
  };

  var selectFirstItemInDropdown = function(inputField) {
    selectItemInDropdown(inputField);
  };

  it('should render input element with given id plus _value', function() {
    expect(element.find('#ex1_value').length).toBe(1);
  });

  it('should render placeholder string', function() {
    expect(element.find('#ex1_value').attr('placeholder')).toEqual('Placeholder text');
  });

  it('should show suggestion box when a letter is typed and narrow down results when more letters are specified', function() {

    $scope.autocompleteData = {
      'key': [
        'key-value1',
        'key-value2'
      ],
      'kay': null,
      'blah': null
    };

    var inputField = element.find('#ex1_value');
    var ek = createKeyEvent(75); // letter: k

    inputField.val('k');
    inputField.trigger('input');
    inputField.trigger(ek);
    $timeout.flush();
    expect(element.find('.angucomplete-title').length).toBe(2);
    expect($(element.find('div.angucomplete-title').get(0)).text()).toBe('key');
    expect($(element.find('div.angucomplete-title').get(1)).text()).toBe('kay');

    var ee = createKeyEvent(69); // letter: e

    inputField.val('e');
    inputField.trigger('input');
    inputField.trigger(ee);
    $timeout.flush();
    expect(element.find('.angucomplete-title').length).toBe(1);
    expect($(element.find('div.angucomplete-title').get(0)).text()).toBe('key');

  });

  it('should allow to select key suggestions from the dropdown', function() {

    $scope.autocompleteData = {
      'key': null,
      'kay': null,
      'blah': null
    };

    var inputField = element.find('#ex1_value');
    var ek = createKeyEvent(75); // letter: k

    inputField.val('k');
    inputField.trigger('input');
    inputField.trigger(ek);
    $timeout.flush();
    expect(element.find('.angucomplete-title').length).toBe(2);

    selectFirstItemInDropdown(inputField);

    expect(inputField.val()).toBe('key="";');
    expect($scope.$root.ex1_searchStr).toBe('key="";');
  });

  it('should allow to select value suggestions in the dropdown for keys which have values', function() {

    $scope.autocompleteData = {
      'key': ['value1', 'value2'],
      'kay': null,
      'blah': null
    };

    var inputField = element.find('#ex1_value');
    var ek = createKeyEvent(75); // letter: k

    inputField.val('k');
    inputField.trigger('input');
    inputField.trigger(ek);
    $timeout.flush();
    expect(element.find('.angucomplete-title').length).toBe(2);

    selectFirstItemInDropdown(inputField);

    expect(inputField.val()).toBe('key="";');

    // flush trigger value autcompletion
    $timeout.flush();
    // flush load value data
    $timeout.flush();

    expect(element.find('.angucomplete-title').length).toBe(2);
    expect($(element.find('div.angucomplete-title').get(0)).text()).toBe('value1');
    expect($(element.find('div.angucomplete-title').get(1)).text()).toBe('value2');

    // select value

    selectFirstItemInDropdown(inputField);

    expect(inputField.val()).toBe('key="value1";');
    expect($scope.$root.ex1_searchStr).toBe('key="value1";');
  });

  it('should allow to use suggestion for more than one key="value" pair', function() {

    $scope.autocompleteData = {
      'key': ['vkey1', 'vkey2'],
      'kay': ['vkay1', 'vkay2'],
      'blah': null
    };

    var inputField = element.find('#ex1_value');
    var ek = createKeyEvent(75); // letter: k

    inputField.val('k');
    inputField.trigger('input');
    inputField.trigger(ek);
    $timeout.flush();
    expect(element.find('.angucomplete-title').length).toBe(2);

    selectFirstItemInDropdown(inputField);

    expect(inputField.val()).toBe('key="";');

    // flush trigger value autcompletion
    $timeout.flush();
    // flush load value data
    $timeout.flush();

    expect(element.find('.angucomplete-title').length).toBe(2);

    // select value

    selectFirstItemInDropdown(inputField);

    expect(inputField.val()).toBe('key="vkey1";');

    // press space to start autocompletion for next key="value"

    var espace = createKeyEvent(32); // space

    inputField.trigger('input');
    inputField.trigger(espace);
    $timeout.flush();

    expect(element.find('.angucomplete-title').length).toBe(3);

    selectItemInDropdown(inputField,2);

    // flush trigger value autcompletion
    $timeout.flush();
    // flush load value data
    $timeout.flush();

    selectItemInDropdown(inputField,2);

    expect(inputField.val()).toBe('key="vkey1"; kay="vkay2";');
    expect($scope.$root.ex1_searchStr).toBe('key="vkey1"; kay="vkay2";');
  });



});

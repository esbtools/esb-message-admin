/**
 * Angucomplete
 * Autocomplete directive for AngularJS
 * By Daryl Rowland
 */

angular.module('angucomplete', [] )
    .directive('angucomplete', function ($parse, $http, $sce, $timeout) {
    return {
        restrict: 'EA',
        scope: {
            "id": "@id",
            "placeholder": "@placeholder",
            "selectedObject": "=selectedobject",
            "userPause": "@pause",
            "localData": "=localdata",
            "inputClass": "@inputclass"
        },
        template: '<div class="angucomplete-holder"><input id="{{id}}_value" ng-model="searchStr" type="text" placeholder="{{placeholder}}" class="{{inputClass}}" onmouseup="this.focus();" ng-focus="resetHideResults()" ng-blur="hideResults()" /><div id="{{id}}_dropdown" class="angucomplete-dropdown" ng-if="showDropdown"><div class="angucomplete-searching" ng-show="searching">Searching...</div><div class="angucomplete-searching" ng-show="!searching && (!results || results.length == 0)">No results found</div><div class="angucomplete-row" ng-repeat="result in results" ng-click="selectResult(result)" ng-mouseover="hoverRow()" ng-class="{\'angucomplete-selected-row\': $index == currentIndex}"><div class="angucomplete-title">{{ result }}</div></div></div></div>',

        link: function($scope, elem, attrs) {
            $scope.lastSearchTerm = null;
            $scope.currentIndex = null;
            $scope.justChanged = false;
            $scope.searchTimer = null;
            $scope.hideTimer = null;
            $scope.searching = false;
            $scope.pause = 500;
            $scope.searchStr = null;
            $scope.autocompletingKeys = true; // false means searching values
            $scope.delimiter=';';

            if ($scope.userPause) {
                $scope.pause = $scope.userPause;
            }

            // extract last key="value" component (those components are delimited by $scope.delimiter)
            // in a form of a {key: 'key', value: 'value'}
            // if value is not present, (i.e. there is only key), return {key: 'key'}
            getLastTerm = function(searchStr) {
                searchStr = searchStr.trim();
                if (searchStr.lastIndexOf($scope.delimiter) == searchStr.length-1) {
                    searchStr = searchStr.substring(0, searchStr.length-2);
                }

                var terms = searchStr.split($scope.delimiter);
                var lastTermStr = terms[terms.length-1].trim();
                var keyValueArray = lastTermStr.split("=");

                var lastTerm = {};
                lastTerm.key=keyValueArray[0];
                if (keyValueArray.length == 2)
                    lastTerm.value=keyValueArray[1].replace(/"/g, '');

                return lastTerm;
            };

            getAllTermsButLast = function(searchStr) {
                var terms = searchStr.split($scope.delimiter);
                terms.pop();
                return terms.join($scope.delimiter);
            };

            $scope.processResults = function(responseData, str) {
                if (responseData && responseData.length > 0) {
                    $scope.results = [];

                    for (var i = 0; i < responseData.length; i++) {
                        $scope.results[$scope.results.length] = responseData[i];
                    }

                } else {
                    $scope.results = [];
                }
            }

            $scope.searchTimerComplete = function(str) {
                // Begin the search
                var wholeStr = str;

                var lastTerm = getLastTerm(str);

                if ($scope.autocompletingKeys) {
                    if (lastTerm.value == undefined) {
                        // use typed key for matching
                        str = lastTerm.key;
                    }
                    else {
                        // don't use last key, b/c we're starting a brand new one
                        str = "";
                    }
                } else {
                    // use typed value for matching
                    str = getLastTerm(str).value;
                }

                if ($scope.localData) {
                    var matches = [];

                    if ($scope.autocompletingKeys) {
                        // autocomplete by key
                        for (var key in $scope.localData) {
                            var match = false;

                            if (key.toLowerCase().indexOf(str.toLowerCase()) >= 0)
                                matches[matches.length] = key;
                        }
                    } else {
                        // autocomplete by value
                        if ($scope.localData[lastTerm.key]) {
                            for (var i = 0; i < $scope.localData[lastTerm.key].length; i++) {
                                if ($scope.localData[lastTerm.key][i].toLowerCase().indexOf(str.toLowerCase()) >= 0) {
                                    matches[matches.length] = $scope.localData[lastTerm.key][i];
                                }
                            }
                        }
                        else {
                            return;
                        }
                    }

                    $scope.searching = false;
                    $scope.processResults(matches, wholeStr);
                }
            }

            $scope.hideResults = function() {
                $scope.hideTimer = $timeout(function() {
                    $scope.showDropdown = false;
                }, $scope.pause);
            };

            $scope.resetHideResults = function() {
                if($scope.hideTimer) {
                    $timeout.cancel($scope.hideTimer);
                };
            };

            $scope.hoverRow = function(index) {
                $scope.currentIndex = index;
            }

            getCaretPosition = function() {
                return document.getElementById($scope.id+'_value').selectionEnd;
            };

            // not trimmed
            getInputSearchVal = function() {
                return $('#'+$scope.id+'_value').val();
            };

            $scope.keyPressed = function(event) {
                if (!(event.which == 38 || event.which == 40 || event.which == 13)) {
                    if (!$scope.searchStr || $scope.searchStr == "") {
                        $scope.showDropdown = false;
                        $scope.lastSearchTerm = null
                    } else {

                        var lastTerm = getLastTerm($scope.searchStr);

                        var searchStr = getInputSearchVal();

                        if (lastTerm.value !== undefined && getCaretPosition() < searchStr.length && getCaretPosition() > searchStr.lastIndexOf('=')) {
                            // search value exists and cursor is not at the end of the search string or before last value (= sign)
                            // --> autocomplete values

                            if (!$scope.localData[lastTerm.key]) {
                                // there is no value autocomplete information for this key
                                event.preventDefault();
                                return;
                            }

                            $scope.autocompletingKeys = false;
                            $scope.lastSearchTerm = lastTerm.value;
                        }
                        else if (getCaretPosition() >= searchStr.lastIndexOf($scope.delimiter)) {
                            // autocomplete keys
                            $scope.autocompletingKeys = true;
                            $scope.lastSearchTerm = lastTerm.key;

                        } else {
                            event.preventDefault();
                            return;
                        }

                        $scope.showDropdown = true;
                        $scope.currentIndex = -1;
                        $scope.results = [];

                        if ($scope.searchTimer) {
                            $timeout.cancel($scope.searchTimer);
                        }

                        $scope.searching = true;

                        $scope.searchTimer = $timeout(function() {
                            $scope.searchTimerComplete($scope.searchStr);
                        }, $scope.pause);
                    }
                } else {
                    event.preventDefault();
                }
            }

            $scope.selectResult = function(result) {
                if ($scope.autocompletingKeys) {
                    // add selected key to the searchStr (input text)
                    var prefix = getAllTermsButLast($scope.searchStr).trim();
                    if (prefix.length > 0)
                        $scope.searchStr = prefix + '; ' + result + '=""'+$scope.delimiter;
                    else
                        $scope.searchStr = result + '=""'+$scope.delimiter;
                } else {
                    // remove current incomplete value
                    var searchStr = $scope.searchStr;
                    searchStr=searchStr.substring(0, searchStr.lastIndexOf('"'));
                    searchStr=searchStr.substring(0, searchStr.lastIndexOf('"')+1);

                    // add value selected from autocompletion list to the last term
                    $scope.searchStr = searchStr + result + '"'+$scope.delimiter;

                }
                $scope.lastSearchTerm = result;
                //$scope.selectedObject = result;
                $scope.showDropdown = false;
                $scope.results = [];

                if ($scope.autocompletingKeys) {
                    $timeout(function() {
                        var input = $('#'+$scope.id+'_value');
                        var pos = input.val().length-2;
                        input.get(0).setSelectionRange(pos,pos);

                        // simulate key press to fire value autocompletion
                        $scope.keyPressed(document.createEvent("KeyboardEvent"));
                    },25);
                }
                //$scope.$apply();
            }

            $scope.$watch('searchStr', function() {
                // put searchStr into root scope
                eval('$scope.$root.'+$scope.id+'_searchStr = $scope.searchStr');
            });

            var inputField = elem.find('input');

            inputField.on('keyup', $scope.keyPressed);

            elem.on("keyup", function (event) {
                if(event.which === 40) { // down arrow
                    if ($scope.results && ($scope.currentIndex + 1) < $scope.results.length) {
                        $scope.currentIndex ++;
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                    $scope.$apply();
                } else if(event.which == 38) { // up arrow
                    if ($scope.currentIndex >= 1) {
                        $scope.currentIndex --;
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                } else if (event.which == 13) { // enter
                    if ($scope.results && $scope.currentIndex >= 0 && $scope.currentIndex < $scope.results.length) {
                        $scope.selectResult($scope.results[$scope.currentIndex]);
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    } else {
                        $scope.results = [];
                        $scope.$apply();
                        event.preventDefault;
                        event.stopPropagation();
                    }

                } else if (event.which == 27) { // esc
                    $scope.results = [];
                    $scope.showDropdown = false;
                    $scope.$apply();
                } else if (event.which == 8) { // backspace
                    //$scope.selectedObject = null;
                    $scope.$apply();
                }
            });

        }
    };
});


var esbMessageAdminServices = angular.module('esbMessageAdminServices', []);

esbMessageAdminServices.service('EsbMessageService', ['$http', '$q', 'Globals', '$log', '$filter', function($http, $q, Globals, $log, $filter) {

    var self = this;

    self.search = function(criteria, fromDate, toDate, start, maxResults, sortField, sortAsc) {
        $log.debug('Search criteria: '+JSON.stringify(criteria));

        // convert to path
        var critPath = "";
        for (var key in criteria) {
            critPath += ";"+key+"="+criteria[key];
        }

        var _fromDate = $filter('date')(fromDate, Globals.dateFormat.service);
        var _toDate = $filter('date')(toDate, Globals.dateFormat.service);

        return $http({method: 'GET', url: "api/search/criteria/crit{critPath}?fromDate={fromDate}&toDate={toDate}&start={start}&results={maxResults}&sortField={sortField}&sortAsc={sortAsc}".
            supplant({critPath: critPath, fromDate: _fromDate, toDate: _toDate, start: start, maxResults: maxResults, sortField: sortField, sortAsc: sortAsc+""})}).
            then(function(results) {
                // TODO: error handling

                // convert dates from string to date objects
                angular.forEach(results.data.messages, function(esbMessage) {
                    esbMessage.timestamp = new Date(esbMessage.timestamp);
                });

                return results;
            });
    };

    self.getMessage = function(id) {
        $log.debug("Fetching message id="+id);

        return $http({method: 'GET', url: "api/search/id/"+id});
    };

    // fetch keys and values used for search query autocompletion
    self.getSuggestions = function() {
        return $http({method: 'GET', url: 'api/key/suggest/', cache: true});
    };

    self.getSyncKeysTree = function() {
        return $http({method: 'GET', url: "api/key/tree/Entities"});
    };

    self.getSearchKeysTree = function() {
        return $http({method: 'GET', url: "api/key/tree/SearchKeys"});
    };

    self.addKey = function(argParentId, argName, argType, argValue) {

        return $http({method: 'PUT', url: "api/key/addChild/{parentId}?name={name}&type={type}&value={value}".
            supplant({parentId: argParentId, name: argName, type: argType, value: argValue })}).
            then(function(results) {

                return results;
            });
            // todo error handling
    };

    self.updateKey = function(argId, argName, argType, argValue) {

        return $http({method: 'PUT', url: "api/key/update/{id}?name={name}&type={type}&value={value}".
            supplant({id: argId, name: argName, type: argType, value: argValue })}).
            then(function(results) {

                return results;
            });
            // todo error handling
    };

    self.deleteKey = function(argId) {

        return $http({method: 'PUT', url: "api/key/delete/{id}".
            supplant({id: argId })}).
            then(function(results) {

                return results;
            });
            // todo error handling
    };

}]);

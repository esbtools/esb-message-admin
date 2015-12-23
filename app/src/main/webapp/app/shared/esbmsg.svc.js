esbMessageAdminApp.service('EsbMessageService',
	[
	    '$http',
	    '$q',
	    'Globals',
	    '$log',
	    '$filter',
	    'messageCenterService',
	    function($http, $q, Globals, $log, $filter, messageCenterService) {
	    	var self = this;

            self.search = function( criteria, fromDate, toDate, start, maxResults, sortField, sortAsc) {
            	$log.debug('Search criteria: ' + JSON.stringify(criteria));

                // convert to path
                var critPath = "";
                for ( var key in criteria) {
                	critPath += ";" + key + "=" + criteria[key];
                }

                var _fromDate = $filter('date')(fromDate, Globals.dateFormat.service);
                var _toDate = $filter('date')(toDate, Globals.dateFormat.service);

                return $http(
	                {
	                	method : 'GET',
	                    url : "api/search/criteria/crit{critPath}?fromDate={fromDate}&toDate={toDate}&start={start}&results={maxResults}&sortField={sortField}&sortAsc={sortAsc}".supplant(
		                    {
		                    	critPath : critPath,
		                    	fromDate : _fromDate,
		                    	toDate : _toDate,
		                    	start : start,
		                    	maxResults : maxResults,
		                    	sortField : sortField,
		                    	sortAsc : sortAsc + ""
		                    }
	                    )
	                }
	            ).then(
	                function(results) {
	                	if (results.data.totalResults == 0) {
	                    	messageCenterService
	                        .add('info','No results found',{timeout : 5000});
	                    } else {
	                    	// convert dates from string to date objects
	                    	angular.forEach(
	                    		results.data.messages,
	                            function(esbMessage) {
	                                esbMessage.timestamp = new Date(esbMessage.timestamp);
	                            }
	                    	);
	                    }
	                	return results;
	                }
	            );
            };

            self.getMessage = function(id) {
            	$log.debug("Fetching message id=" + id);
                return $http(
                	{
                		method : 'GET',
                        url : "api/search/id/" + id
                    }
                );
            };

            // fetch keys and values used for search query autocompletion
            self.getSuggestions = function() {
            	return $http(
            		{
            			method : 'GET',
                        url : 'api/key/suggest/',
                        cache : true
                    }
            	);
            };

            self.getSyncKeysTree = function() {
            	return self.getTree("Entities");
            };

            self.getSearchKeysTree = function() {
            	return self.getTree("SearchKeys");
            };

            self.respondSuccess = function(argResponse) {
            	if (argResponse.status === "Success") {
            		return argResponse.tree;
                } else {
                	messageCenterService.add('danger',argResponse.errorMessage,{status : messageCenterService.status.permanent});
                    return null;
                }
            }

            self.respondError = function() {
            	messageCenterService.add('danger','Unable to contact server!',{status : messageCenterService.status.permanent});
                return null;
            }

            self.getTree = function(argType) {
            	return $http.get("api/key/tree/" + argType).success(
            		function(response) {
            			self.respondSuccess(response);
                    }
            	).error(
            		function() {
            			self.respondError();
                    }
            	);
            };

            self.addKey = function(argParentId, argName, argType, argValue) {
            	return $http.post("api/key/addChild/{parentId}?name={name}&type={type}&value={value}".supplant(
	            		{
	            			parentId : argParentId,
	            			name : argName,
	                        type : argType,
	                        value : argValue
	                    }
            		)
            	).success(
            	function(response) {
            		self.respondSuccess(response);
                }
	            ).error(
	            	function() {
	            		self.respondError();
	            	}
	            );
            };

            self.updateKey = function(argId, argName, argType, argValue) {
            	return $http.put("api/key/update/{id}?name={name}&type={type}&value={value}".supplant(
            			{
            				id : argId,
                            name : argName,
                            type : argType,
                            value : argValue
                        }
            		)
            	).success(
            		function(response) {
            			self.respondSuccess(response);
                    }
            	).error(
            		function() {
            			self.respondError();
                    }
            	);
            };

            self.deleteKey = function(argId) {
            	return $http.delete("api/key/{id}".supplant({id : argId})).success(
        			function(response) {
                		self.respondSuccess(response);
                	}
                ).error(
                	function() {
                		self.respondError();
                	}
                );
            };

            self.sync = function(argEntity, argSystem, argKey, argValues) {

		    	return $http.post("api/key/sync/{entity}/{system}/{key}?values={values}".supplant(
	                	{
	                		entity : argEntity,
	                        system : argSystem,
	                        key : argKey,
	                        values : argValues
	                    }
	                )
		         ).then(
		        	function(results) {
		        		if (results.data.status === "Success") {
		        			messageCenterService.add('success', 'Sync was triggered succesfully!', {timeout : 3000});
		                } else {
		                	messageCenterService.add('danger', response.data.errorMessage, {status : messageCenterService.status.permanent});
		                }
		            },
		            function(err) {
		                self.respondError();
		            }
		        );
            };
        }
    ]
);

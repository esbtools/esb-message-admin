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

esbMessageAdminApp.service('localStorage',
	function() {
    	return window.localStorage;
	}
);

esbMessageAdminApp.service('errorColumnPrefs',
	[
	 	'localStorage',
	 	function(localStorage) {
	 		var self = this;
	 		var error_column_storage_key = 'esb_message_admin.error_columns';
	 		var persistent = ['field', 'visible'];
	 		var standardCellTemplate = '<div class="ngCellText" ng-class="col.colIndex()" title="{{row.getProperty(col.field)}}">{{row.getProperty(col.field)}}</div>';
	 		var dateCellTempate = '<div class="ngCellText" ng-class="col.colIndex()">{{row.getProperty(col.field) | date:"M/d/yy HH:mm:ss"}}</div>';
		    self.defaults = [
		        // FIXME should include business key (issue #59)
		        {
		            field : 'sourceSystem',
		            displayName : 'Source',
		            width : '****',
		            cellTemplate : standardCellTemplate,
		        },
		        {
		            field : 'errorSystem',
		            displayName : 'Error System',
		            width : '****',
		            cellTemplate : standardCellTemplate,
		        },
		        {
		            field : 'messageType',
		            displayName : 'Type',
		            width : '****',
		            cellTemplate : standardCellTemplate,
		        },
		        {
		            field : 'errorType',
		            displayName : 'Error Type',
		            width : '****',
		            cellTemplate : standardCellTemplate,
		            visible: false,
		        },
		        {
		            field : 'timestamp',
		            displayName : 'Timestamp',
		            width : '****',
		            cellTemplate : dateCellTempate,
		        },
		        {
		            field : 'occurrenceCount',
		            displayName : '#',
		            width : '*',
		            cellTemplate : standardCellTemplate,
		        },
		    ];

		    self.default_map = {};
		    for (var i in self.defaults) {
		        var column = self.defaults[i];
		        self.default_map[column.field] = column;
		    };

		    self.save = function(columns) {
		        var sanitized_columns = [];
		        for (var i in columns) {
		            var column = columns[i];
		            var sanitized_column = {};
		            for (var i in persistent) {
		                var property = persistent[i];
		                sanitized_column[property] = column[property];
		            }
		            sanitized_columns.push(sanitized_column);
		        }
		        localStorage.setItem(error_column_storage_key, JSON.stringify(sanitized_columns));
		    };

		    function add_missing_fields(columns, loaded) {
		        for (var field_name in self.default_map) {
		            if (!(field_name in loaded)) {
		                columns.push(self.default_map[field_name]);
		            }
		        }
		    }

		    function get_defaults(name) {
		        var column = {};
		        var default_setting = self.default_map[name];
		        for (var property in default_setting) {
		            column[property] = default_setting[property];
		        }
		        return column;
		    }

		    function merge_persistent_settings(destination, source) {
		        for (var i in persistent) {
		            var property = persistent[i];
		            destination[property] = source[property];
		        }
		    }

		    function sanitize(columns) {
		        var sanitized_columns = [];
		        var loaded = {};
		        for (var i in columns) {
		            var column = columns[i];
		            var sanitized_column = get_defaults(column.field);
		            merge_persistent_settings(sanitized_column, column);
		            sanitized_columns.push(sanitized_column);
		            loaded[column.field] = true;
		        }
		        add_missing_fields(sanitized_columns, loaded);

		        return sanitized_columns;
		    }

		    this.load = function() {
		        var saved = localStorage.getItem(error_column_storage_key);
		        if (saved) {
		            return sanitize(JSON.parse(saved));
		        }
		        return self.defaults;
		    }
		}
	]
);

esbMessageAdminApp.service('ngGridLayoutPlugin', ngGridLayoutPlugin);

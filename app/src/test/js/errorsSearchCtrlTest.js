describe(
        'ErrorsSearchCtrl',
        function() {

            // Instantiate a new version of my module before each test
            beforeEach(module('esbMessageAdminApp'));

            var errorsSearchCtrl, http, globals, scope, rootScope, esbMessageService, filter;

            var toDate, fromDate, toDateStr, fromDateStr;

            var noResultsResponse = {
                "totalResults" : 0,
                "itemsPerPage" : 0,
                "page" : 0,
                "messages" : null
            };

            beforeEach(inject(function($rootScope, $controller, $httpBackend,
                    Globals, EsbMessageService, $filter) {
                rootScope = $rootScope;
                scope = $rootScope.$new();
                http = $httpBackend;
                globals = Globals;
                esbMessageService = EsbMessageService;
                filter = $filter;

                toDate = new Date(2015, 0, 15, 0, 0, 0);

                fromDate = new Date(toDate.getTime());
                fromDate.setDate(fromDate.getDate() - 1); // yesterday

                toDateStr = filter('date')(toDate, globals.dateFormat.service);
                fromDateStr = filter('date')(fromDate,
                        globals.dateFormat.service);

                // spyOn(MessageService, 'addFailureMessage').andCallThrough();
                // spyOn(termsService, 'updateTerm').andCallThrough();

                (errorsSearchCtrl) = $controller('ErrorsSearchCtrl', {
                    '$scope' : scope
                });

                // not testing autcompletion stuff - those unit tests belong to
                // angucomplete-keyvalue project
                http.expectGET("api/search/suggest/").respond(200);
                // Simulate a server response
                http.flush();
            }));

            it(
                    'makes a GET request with parameters built from search criteria',
                    function() {

                        rootScope.searchField_searchStr = 'Key="Value"; Blah=" Bleh ";   Foo="bar"  ; invalid';
                        scope.toDate = toDate;
                        scope.fromDate = fromDate;

                        scope.search();

                        var url = "api/search/criteria/crit{crit}?fromDate={fromDate}&toDate={toDate}&start=0&results=12&sortField=timestamp&sortAsc=true"
                                .supplant({
                                    crit : ';Key=Value;Blah=Bleh;Foo=bar',
                                    fromDate : fromDateStr,
                                    toDate : toDateStr
                                });

                        http.expectGET(url).respond(200, noResultsResponse);
                        // Simulate a server response
                        http.flush();

                    });

            afterEach(function() {
                // Ensure that all expects set on the $httpBackend
                // were actually called
                http.verifyNoOutstandingExpectation();

                // Ensure that all requests to the server
                // have actually responded (using flush())
                http.verifyNoOutstandingRequest();
            });

        });
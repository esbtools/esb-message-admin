describe(
        'SyncKeysCtrl and SearchKeysCtrl',
        function() {

            beforeEach(module('esbMessageAdminApp'));

            var syncKeysCtrl, searchKeysCtrl, http, globals, scope, rootScope, esbMessageService, filter;

            beforeEach(inject(function($rootScope, $controller, $httpBackend,
                    Globals, EsbMessageService, $filter) {
                rootScope = $rootScope;
                syncKeysScope = $rootScope.$new();
                searchKeysScope = $rootScope.$new();
                http = $httpBackend;
                globals = Globals;
                esbMessageService = EsbMessageService;
                filter = $filter;

                (syncKeysCtrl) = $controller('SyncKeysCtrl', {
                    '$scope' : syncKeysScope
                });
                http.expectGET("api/key/tree/Entities").respond(
                        syncKeysSuccessResponse);
                http.flush();

                (searchKeysCtrl) = $controller('SearchKeysCtrl', {
                    '$scope' : searchKeysScope
                });
                http.expectGET("api/key/tree/SearchKeys").respond(
                        searchKeysSuccessResponse);
                http.flush();

            }));

            function loadTest(scope, response) {
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.tree);
                expect(scope.parent).toEqual(response.tree);
            }

            it('loads Sync and Search Keys Tab', function() {
                loadTest(syncKeysScope, syncKeysSuccessResponse);
                loadTest(searchKeysScope, searchKeysSuccessResponse);
            });

            function crumbTest(scope, response) {
                expect(scope.crumbs.length).toEqual(1);
                expect(scope.crumbs[0]).toEqual(response.tree);
                expect(scope.parent).toEqual(response.tree);
                scope.manageChildren(scope.parent.children[0]);

                expect(scope.crumbs.length).toEqual(2);
                expect(scope.crumbs[0]).toEqual(response.tree);
                expect(scope.crumbs[1]).toEqual(response.tree.children[0]);
                expect(scope.parent).toEqual(response.tree.children[0]);

                scope.manageChildren(scope.parent.children[0]);
                expect(scope.crumbs.length).toEqual(3);
                expect(scope.crumbs[0]).toEqual(response.tree);
                expect(scope.crumbs[1]).toEqual(response.tree.children[0]);
                expect(scope.crumbs[2]).toEqual(
                        response.tree.children[0].children[0]);
                expect(scope.parent).toEqual(
                        response.tree.children[0].children[0]);

                scope.gotoCrumb(scope.crumbs[0]);
                expect(scope.crumbs.length).toEqual(1);
                expect(scope.crumbs[0]).toEqual(response.tree);
                expect(scope.parent).toEqual(response.tree);
            }

            it('crumb and manage chidren tests', function() {
                crumbTest(syncKeysScope, syncKeysSuccessResponse);
                crumbTest(searchKeysScope, searchKeysSuccessResponse);
            });

            function addTest(scope, response, name, type, value) {
                expect(scope.addMode).toEqual(false);
                expect(scope.crumbs.length).toEqual(1);
                scope.addChild(scope.parent);
                expect(scope.addMode).toEqual(true);
                expect(scope.crumbs.length).toEqual(2);
                scope.addFormName = name;
                scope.addFormValue = value;
                scope.requestAdd();
                http.expectPOST(
                        "api/key/addChild/" + scope.parent.id + "?name=" + name
                                + "&type=" + type + "&value=" + value).respond(
                        response);
                http.flush();
                expect(scope.parent).toEqual(response.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.tree);
            }

            it('add tests', function() {
                addTest(syncKeysScope, syncKeysSuccessResponse, "testName",
                        "Entity", "testValue");
                addTest(searchKeysScope, searchKeysSuccessResponse,
                        "SearchKeys", "SearchKey", "testValue");
            });

            function updateTests(scope, response, name, type, value) {
                expect(scope.updateMode).toEqual(false);
                expect(scope.crumbs.length).toEqual(1);
                scope.editChild(scope.parent.children[0]);
                expect(scope.updateMode).toEqual(true);
                expect(scope.crumbs.length).toEqual(2);
                expect(scope.parent).toEqual(response.tree.children[0]);
                scope.parent.name = name;
                scope.parent.value = value;
                scope.requestUpdate();
                http.expectPUT(
                        "api/key/update/" + scope.parent.id + "?name=" + name
                                + "&type=" + type + "&value=" + value).respond(
                        response);
                http.flush();
                expect(scope.parent).toEqual(response.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.tree);
            }

            it('update tests', function() {
                updateTests(syncKeysScope, syncKeysSuccessResponse, "testName",
                        "Entity", "testValue");
                updateTests(searchKeysScope, searchKeysSuccessResponse,
                        "SearchKey", "SearchKey", "testValue");
            });

            function deleteTests(scope, response) {
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.tree);
                expect(scope.parent).toEqual(response.tree);
                scope.deleteChild(response.tree.children[0]);
                http.expectDELETE("api/key/" + response.tree.children[0].id)
                        .respond(response);
                http.flush();
                expect(scope.parent).toEqual(response.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.tree);
            }

            it('delete tests', function() {
                deleteTests(syncKeysScope, syncKeysSuccessResponse);
                deleteTests(searchKeysScope, searchKeysSuccessResponse);
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

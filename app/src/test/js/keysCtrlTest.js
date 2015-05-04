describe(
        'SyncKeysCtrl and SearchKeysCtrl',
        function() {

            beforeEach(module('esbMessageAdminApp', function($provide) {
                $provide.value("EsbMessageService", mockService);
            }));

            var syncKeysCtrl, searchKeysCtrl, http, globals, scope, rootScope, esbMessageService, filter;

            beforeEach(inject(function($rootScope, $controller, $httpBackend,
                    Globals, EsbMessageService, $filter, _$q_) {
                rootScope = $rootScope;
                syncKeysScope = $rootScope.$new();
                searchKeysScope = $rootScope.$new();
                http = $httpBackend;
                globals = Globals;
                esbMessageService = EsbMessageService;
                filter = $filter;
                $q = _$q_;
                (syncKeysCtrl) = $controller('SyncKeysCtrl', {
                    '$scope' : syncKeysScope
                });
                deferred.resolve(syncKeysSuccessResponse);

                (searchKeysCtrl) = $controller('SearchKeysCtrl', {
                    '$scope' : searchKeysScope
                });
                deferred.resolve(searchKeysSuccessResponse);
                $rootScope.$apply();

            }));

            it('loads Sync and Search Keys Tab', function() {
                loadTest(syncKeysScope, syncKeysSuccessResponse);
                loadTest(searchKeysScope, searchKeysSuccessResponse);
            });

            function loadTest(scope, response) {
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.data.tree);
                expect(scope.parent).toEqual(response.data.tree);
            }

            it('crumb tests', function() {
                crumbTest(syncKeysScope, syncKeysSuccessResponse);
                crumbTest(searchKeysScope, searchKeysSuccessResponse);
            });

            function crumbTest(scope, response) {
                expect(scope.crumbs.length).toEqual(1);
                expect(scope.crumbs[0]).toEqual(response.data.tree);
                expect(scope.parent).toEqual(response.data.tree);

                scope.gotoCrumb(scope.crumbs[0]);
                expect(scope.crumbs.length).toEqual(1);
                expect(scope.crumbs[0]).toEqual(response.data.tree);
                expect(scope.parent).toEqual(response.data.tree);
            }

            it('manage chidren tests', function() {
                manageChildrenTest(syncKeysScope, syncKeysSuccessResponse);
                manageChildrenTest(searchKeysScope, searchKeysSuccessResponse);
            });

            function manageChildrenTest(scope, response) {
                expect(scope.crumbs.length).toEqual(1);
                expect(scope.crumbs[0]).toEqual(response.data.tree);
                expect(scope.parent).toEqual(response.data.tree);
                scope.manageChildren(scope.parent.children[0]);

                expect(scope.crumbs.length).toEqual(2);
                expect(scope.crumbs[0]).toEqual(response.data.tree);
                expect(scope.crumbs[1]).toEqual(response.data.tree.children[0]);
                expect(scope.parent).toEqual(response.data.tree.children[0]);

                scope.manageChildren(scope.parent.children[0]);
                expect(scope.crumbs.length).toEqual(3);
                expect(scope.crumbs[0]).toEqual(response.data.tree);
                expect(scope.crumbs[1]).toEqual(response.data.tree.children[0]);
                expect(scope.crumbs[2]).toEqual(
                        response.data.tree.children[0].children[0]);
                expect(scope.parent).toEqual(
                        response.data.tree.children[0].children[0]);
            }

            it('add tests', function() {
                addTest(syncKeysScope, syncKeysSuccessResponse, "testName",
                        "Entity", "testValue");
                addTest(searchKeysScope, searchKeysSuccessResponse,
                        "SearchKeys", "SearchKey", "testValue");
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
                deferred.resolve(response);
                rootScope.$apply();
                expect(scope.parent).toEqual(response.data.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.data.tree);
            }

            it('update tests', function() {
                updateTests(syncKeysScope, syncKeysSuccessResponse, "testName",
                        "Entity", "testValue");
                updateTests(searchKeysScope, searchKeysSuccessResponse,
                        "SearchKey", "SearchKey", "testValue");
            });

            function updateTests(scope, response, name, type, value) {
                expect(scope.updateMode).toEqual(false);
                expect(scope.crumbs.length).toEqual(1);
                scope.editChild(scope.parent.children[0]);
                expect(scope.updateMode).toEqual(true);
                expect(scope.crumbs.length).toEqual(2);
                expect(scope.parent).toEqual(response.data.tree.children[0]);
                scope.parent.name = name;
                scope.parent.value = value;
                scope.requestUpdate();
                deferred.resolve(response);
                rootScope.$apply();
                expect(scope.parent).toEqual(response.data.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.data.tree);
            }

            it('delete tests', function() {
                deleteTests(syncKeysScope, syncKeysSuccessResponse);
                deleteTests(searchKeysScope, searchKeysSuccessResponse);
            });

            function deleteTests(scope, response) {
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.data.tree);
                expect(scope.parent).toEqual(response.data.tree);
                scope.deleteChild(response.data.tree.children[0]);
                deferred.resolve(response);
                rootScope.$apply();
                expect(scope.parent).toEqual(response.data.result);
                expect(scope.entities || scope.searchKeys).toEqual(
                        response.data.tree);
            }

            afterEach(function() {
                // Ensure that all expects set on the $httpBackend
                // were actually called
                http.verifyNoOutstandingExpectation();

                // Ensure that all requests to the server
                // have actually responded (using flush())
                http.verifyNoOutstandingRequest();
            });

        });

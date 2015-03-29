describe('SyncCtrl', function() {

    // Instantiate a new version of my module before each test
    beforeEach(module('esbMessageAdminApp'));

    var syncCtrl, http, globals, scope, rootScope, esbMessageService, filter;

    beforeEach(inject(function($rootScope, $controller, $httpBackend, Globals,
            EsbMessageService, $filter) {
        rootScope = $rootScope;
        scope = $rootScope.$new();
        http = $httpBackend;
        globals = Globals;
        esbMessageService = EsbMessageService;
        filter = $filter;

        (syncCtrl) = $controller('SyncCtrl', {
            '$scope' : scope
        });

        http.expectGET("api/key/tree/Entities")
                .respond(entitiesSuccessResponse);
        // Simulate a server response
        http.flush();
    }));

    it('loads Sync Tab', function() {
        expect(scope.entities).toEqual(entitiesSuccessResponse.tree);
    });

    it('selects key', function() {

        scope.syncEntity = entitiesSuccessResponse.tree.children[0];
        expect(scope.systems.length).toEqual(0);
        scope.entityChange();
        expect(scope.systems).toEqual(
                entitiesSuccessResponse.tree.children[0].children);
        scope.syncSystem = scope.systems[0];
        expect(scope.keys.length).toEqual(0);
        scope.systemChange();
        expect(scope.keys).toEqual(
                entitiesSuccessResponse.tree.children[0].children[0].children);

    });

    it('enable submit button tests', function() {

        scope.syncEntity = entitiesSuccessResponse.tree.children[0];
        scope.syncSystem = scope.syncEntity.children[0];
        scope.syncKey = scope.syncSystem.children[0];
        expect(scope.enableSubmit()).toEqual(true);
        scope.syncValues = [ "12" ];
        expect(scope.enableSubmit()).toEqual(false);

    });

    it('sync trigger tests', function() {

        scope.syncEntity = entitiesSuccessResponse.tree.children[0];
        scope.syncSystem = scope.syncEntity.children[0];
        scope.syncKey = scope.syncSystem.children[0];
        scope.syncValues = [ "12", "21" ];
        scope.sync();
        http.expectPOST(
                "api/key/sync/" + scope.syncEntity.value + "/"
                        + scope.syncSystem.value + "/" + scope.syncKey.value
                        + "?values=12,21,").respond(syncSuccessResponse);
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
describe('SyncKeysCtrl', function() {

    beforeEach(module('esbMessageAdminApp', function($provide) {
        $provide.value("EsbMessageService", mockService);
    }));

    var syncKeysCtrl, rootScope;

    beforeEach(inject(function($rootScope, $controller, _$q_) {
        rootScope = $rootScope;
        syncKeysScope = $rootScope.$new();
        $q = _$q_;
        spyOn(mockService, "getSyncKeysTree").and.returnValue($q
                .when(syncKeysSuccessResponse));
        syncKeysCtrl = $controller('SyncKeysCtrl', {
            '$scope' : syncKeysScope
        });
        rootScope.$apply();

    }));

    it('loads Tab', function() {
        loadTest(syncKeysScope, syncKeysSuccessResponse);
    });

    it('crumbs', function() {
        crumbTest(syncKeysScope, syncKeysSuccessResponse);
    });

    it('manages chidren', function() {
        manageChildrenTest(syncKeysScope, syncKeysSuccessResponse);
    });

    it('adds key', function() {
        addTest(rootScope, syncKeysScope, syncKeysSuccessResponse, "testName",
                "Entity", "testValue");
    });

    it('updates key', function() {
        updateTests(rootScope, syncKeysScope, syncKeysSuccessResponse,
                "testName", "Entity", "testValue");
    });

    it('deletes key', function() {
        deleteTests(rootScope, syncKeysScope, syncKeysSuccessResponse);
    });

});

describe('SearchKeysCtrl', function() {

    beforeEach(module('esbMessageAdminApp', function($provide) {
        $provide.value("EsbMessageService", mockService);
    }));

    var searchKeysCtrl, rootScope;

    beforeEach(inject(function($rootScope, $controller, _$q_) {
        rootScope = $rootScope;
        searchKeysScope = $rootScope.$new();
        $q = _$q_;
        spyOn(mockService, "getSearchKeysTree").and.returnValue($q
                .when(searchKeysSuccessResponse));
        searchKeysCtrl = $controller('SearchKeysCtrl', {
            '$scope' : searchKeysScope
        });
        rootScope.$apply();

    }));

    it('loads Tab', function() {
        loadTest(searchKeysScope, searchKeysSuccessResponse);
    });

    it('crumbs', function() {
        crumbTest(searchKeysScope, searchKeysSuccessResponse);
    });

    it('manages chidren', function() {
        manageChildrenTest(searchKeysScope, searchKeysSuccessResponse);
    });

    it('adds key', function() {
        addTest(rootScope, searchKeysScope, searchKeysSuccessResponse,
                "SearchKeys", "SearchKey", "testValue");
    });

    it('updates key', function() {
        updateTests(rootScope, searchKeysScope, searchKeysSuccessResponse,
                "SearchKey", "SearchKey", "testValue");
    });

    it('deletes key', function() {
        deleteTests(rootScope, searchKeysScope, searchKeysSuccessResponse);
    });

});

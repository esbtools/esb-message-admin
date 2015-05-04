describe('EsbMessageService', function() {

    // Instantiate a new version of my module before each test
    beforeEach(module('esbMessageAdminApp'));

    var http, esbMessageService;

    beforeEach(inject(function($httpBackend, EsbMessageService) {
        http = $httpBackend;
        esbMessageService = EsbMessageService;
    }));

    it('get sync keys test', function() {
        esbMessageService.getSyncKeysTree();
        http.expectGET("api/key/tree/Entities")
                .respond(syncKeysSuccessResponse);
        http.flush();
    });

    it('get search keys test', function() {
        esbMessageService.getSearchKeysTree();
        http.expectGET("api/key/tree/SearchKeys").respond(
                syncKeysSuccessResponse);
        http.flush();
    });

    it('add keys test', function() {
        esbMessageService.addKey(1, 'name', 'type', 'value');
        http.expectPOST("api/key/addChild/1?name=name&type=type&value=value")
                .respond(syncKeysSuccessResponse);
        http.flush();
    });

    it('udpate keys test', function() {
        esbMessageService.updateKey(1, 'name', 'type', 'value');
        http.expectPUT("api/key/update/1?name=name&type=type&value=value")
                .respond(syncKeysSuccessResponse);
        http.flush();
    });

    it('delete keys test', function() {
        esbMessageService.deleteKey(1);
        http.expectDELETE("api/key/1").respond(syncKeysSuccessResponse);
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
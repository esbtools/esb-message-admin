describe("EsbMessageService", function() {
  var EsbMessageService;
  var $httpBackend;

  var resubmitRequest = {
    "id": 1,
    "payload": {
      "body": "<foo>bar</foo>"
    }
  };

  var resubmitResponse = {
    "status": "Status"
  };

  beforeEach(module("esbMessageAdminApp"));

  beforeEach(inject(function(_$httpBackend_, _EsbMessageService_){
    $httpBackend = _$httpBackend_;
    EsbMessageService = _EsbMessageService_;
  }));

  describe("after calling resubmit", function(){

    it("should POST resubmitted payload to api/key/resubmit/{id} and return the response", function() {
      $httpBackend.expectPOST("api/key/resubmit/1", resubmitRequest.payload).respond(200, resubmitResponse);
      EsbMessageService.resubmitMessage(resubmitRequest)
        .then(function(response) {
        expect(response.data).toEqual(resubmitResponse);}
      );

      $httpBackend.flush();
    });

  });

});
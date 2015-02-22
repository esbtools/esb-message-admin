package org.esbtools.message.admin.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.Header;
import org.esbtools.message.admin.model.HeaderType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchField;
import org.esbtools.message.admin.model.SearchResult;
import org.esbtools.message.admin.model.EsbMessage.ErrorType;
import org.junit.Assert;
import org.junit.Test;

/**
 * ESBMessageAdmin Service Unit Tests
 * @author ykoer
 *
 */
public class ErrorServiceTest extends EsbMessageAdminTestBase {

    @Test
    public void testPersistErrorMessage() {
        EsbMessage esbMessage1 = createTestMessage(0, 0);
        esbMessage1.setSourceSystem("SRC");
        esbMessage1.setHeaders(Arrays.asList(new Header(HeaderType.JMS, "esbPayloadHash", "hash0")));

        EsbMessage esbMessage2 = createTestMessage(1, 0);
        esbMessage2.setSourceSystem("SRC");
        esbMessage2.setHeaders(Arrays.asList(new Header(HeaderType.JMS, "esbPayloadHash", "hash0")));

        EsbMessage esbMessage3 = createTestMessage(2, 0);
        esbMessage3.setSourceSystem("SRC");
        esbMessage3.setHeaders(Arrays.asList(new Header(HeaderType.JMS, "esbPayloadHash", "hash1")));

        EsbMessage esbMessage4 = createTestMessage(3, 0);
        esbMessage4.setSourceSystem("SRC");
        esbMessage4.setHeaders(Arrays.asList(new Header(HeaderType.JMS, "esbPayloadHash", "hash0")));

        // persist the messages
        try {
            service.persist(esbMessage1);
            service.persist(esbMessage2);
            service.persist(esbMessage3);
            service.persist(esbMessage4);
        } catch (IOException e) {
            Assert.fail();
        }

        // test searching by Message Guid
        SearchCriteria criteria1 = new SearchCriteria();
        Criterion[] c1 = {new Criterion(SearchField.sourceSystem, "SRC")};
        criteria1.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, 0, 10);

        Assert.assertEquals(2, result.getTotalResults());
        Assert.assertEquals(1, result.getMessages()[0].getOccurrenceCount());
        Assert.assertEquals(3, result.getMessages()[1].getOccurrenceCount());
    }

    @Test
    public void testSearchByFields() {
        EsbMessage esbMessage = createTestMessage(0, 1);

        // persist the message
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        // test searching by Message Guid
        SearchCriteria criteria1 = new SearchCriteria();
        Criterion[] c1 = {new Criterion(SearchField.messageGuid, "MessageGuid0")};
        criteria1.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, 0, 10);
        Assert.assertEquals(1, result.getTotalResults());

        // test searching by Message Guid and Queue
        SearchCriteria criteria2 = new SearchCriteria();
        Criterion[] c2 = {
                new Criterion(SearchField.messageGuid, "MessageGuid0"),
                new Criterion(SearchField.sourceSystem, "SourceSystem0")
        };
        criteria2.setCriteria(c2);
        result = service.searchMessagesByCriteria(criteria2, null, null, 0, 10);
        Assert.assertEquals(1, result.getTotalResults());

        // test searching by header name and value
        SearchCriteria criteria3 = new SearchCriteria();
        Criterion[] c3 = {
                new Criterion("header0_0", "value0_0"),
        };
        criteria3.setCriteria(c3);
        result = service.searchMessagesByCriteria(criteria3, null, null, 0, 10);
        Assert.assertEquals(1, result.getTotalResults());

    }

    private EsbMessage createTestMessage(int i, int headerCount) {
         EsbMessage esbMessage = new EsbMessage();
         esbMessage.setErrorQueue("ErrorQueue"+i);
         esbMessage.setMessageId("MessageId"+i);
         esbMessage.setTimestamp(new Date());
         esbMessage.setMessageGuid("MessageGuid"+i);
         esbMessage.setMessageType("MessageType"+i);
         esbMessage.setSourceQueue("SourceQueue"+i);
         esbMessage.setSourceLocation("SourceLocation+i");
         esbMessage.setSourceSystem("SourceSystem"+i);
         esbMessage.setOriginalSystem("OriginalSystem"+i);
         esbMessage.setServiceName("ServiceName"+i);
         esbMessage.setErrorComponent("ErrorComponent"+i);
         esbMessage.setErrorMessage("ErrorMessage"+i);
         esbMessage.setErrorDetails("ErrorDetails"+i);
         esbMessage.setErrorSystem("ErrorSystem"+i);
         esbMessage.setErrorType(ErrorType.DATA_ERROR);
         esbMessage.setPayload("<Payload></Payload>");

         List<Header> headers = new ArrayList<Header>();
         for (int j=0; j<headerCount; j++) {
             headers.add(new Header(HeaderType.JMS, "header"+i+"_"+j , "value"+i+"_"+j));
         }
         esbMessage.setHeaders(headers);

         return esbMessage;
    }

}

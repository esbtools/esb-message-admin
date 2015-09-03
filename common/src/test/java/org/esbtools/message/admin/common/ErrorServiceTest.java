/*
 Copyright 2015 esbtools Contributors and/or its affiliates.

 This file is part of esbtools.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.esbtools.message.admin.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.esbtools.message.admin.common.orm.EsbMessageSensitiveInfoEntity;
import org.esbtools.message.admin.common.utility.EncryptionUtility;
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
        SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, null, true, 0, 10);

        Assert.assertEquals(2, result.getTotalResults());
        Assert.assertEquals(1, result.getMessages()[0].getOccurrenceCount());
        Assert.assertEquals(3, result.getMessages()[1].getOccurrenceCount());
    }

    @Test
    public void testSearchByMessageGuidFieldWithSameCase() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria1 = new SearchCriteria();
        Criterion[] c1 = {new Criterion(SearchField.messageGuid, "MessageGuid0")};
        criteria1.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }

    @Test
    public void testSearchByMessageGuidAndQueueFieldsWithSameCase() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria2 = new SearchCriteria();
        Criterion[] c2 = { new Criterion(SearchField.messageGuid, "MessageGuid0"), new Criterion(SearchField.sourceSystem, "SourceSystem0") };
        criteria2.setCriteria(c2);
        SearchResult result = service.searchMessagesByCriteria(criteria2, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }

    @Test
    public void testSearchByHeaderFieldsWithSameCase() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria3 = new SearchCriteria();
        Criterion[] c3 = { new Criterion("header0_0", "value0_0"), };
        criteria3.setCriteria(c3);
        SearchResult result = service.searchMessagesByCriteria(criteria3, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }


    @Test
    public void testSearchByMessageGuidFieldCaseInsensitive() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria1 = new SearchCriteria();
        Criterion[] c1 = {new Criterion(SearchField.messageGuid, "messageguid0")};
        criteria1.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }

    @Test
    public void testSearchByMessageGuidAndQueueFieldsCaseInsensitive() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria2 = new SearchCriteria();
        Criterion[] c2 = {
                new Criterion(SearchField.messageGuid, "messageguid0"),
                new Criterion(SearchField.sourceSystem, "sourcesystem0")
        };
        criteria2.setCriteria(c2);
        SearchResult result = service.searchMessagesByCriteria(criteria2, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }

    @Test
    public void testSearchByHeaderFieldsCaseInsensitive() {
        EsbMessage esbMessage = createTestMessage(0, 1);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria3 = new SearchCriteria();
        Criterion[] c3 = {
                new Criterion("HEADER0_0", "VALUE0_0"),
        };
        criteria3.setCriteria(c3);
        SearchResult result = service.searchMessagesByCriteria(criteria3, null, null, null, true, 0, 10);

        Assert.assertEquals(1, result.getTotalResults());
    }

    @Test
    public void testSegregateSensitiveInfoFromXML() {
        EsbMessage esbMessage = createTestMessage(154, 0);
        String payload = "<Payload><Hello> is it me you're looking for ?</Hello>"+
                "<Example>I can see it in your eyes</Example><Example>I can see it in your soul</Example></Payload>";
        esbMessage.setPayload(payload);
        esbMessage.setSourceSystem("PartiaSourceSystemOne");
        esbMessage.setMessageType("PartialEntityOne");
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }
        Criterion[] c1 = {new Criterion(SearchField.sourceSystem, "PartiaSourceSystemOne")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);
        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("<Payload><Hello> is it me you're looking for ?</Hello>"+
                "<Example>Sensitive Information is not viewable</Example>"+
                "<Example>Sensitive Information is not viewable</Example></Payload>", result.getMessages()[0].getPayload());
        Query query = getEntityManager().createQuery("select f from EsbMessageSensitiveInfoEntity f");
        List<EsbMessageSensitiveInfoEntity> queryResult = (List<EsbMessageSensitiveInfoEntity>) query.getResultList();
        Assert.assertEquals("two sensitive bits expected", 2, queryResult.size());
        EncryptionUtility util = new EncryptionUtility("passwordpassword");
        Assert.assertEquals("<Example>I can see it in your eyes</Example>",util.decrypt(queryResult.get(0).getValue()));
        Assert.assertEquals("<Example>I can see it in your soul</Example>",util.decrypt(queryResult.get(1).getValue()));
        Assert.assertEquals(result.getMessages()[0].getId(),queryResult.get(1).getEsbMessage().getId().longValue());
    }

    @Test
    public void testSegregateSensitiveInfoFromXMLWithFormatting() {
        EsbMessage esbMessage = createTestMessage(255, 0);
        String payload = "<Payload>\n\r\t<Hello> is it me you're looking for ?</Hello>\n\r\t"+
                "<Example>I can see it in your eyes</Example>\n<Example>I can see it in your soul</Example>\n</Payload>";
        esbMessage.setPayload(payload);
        esbMessage.setSourceSystem("PartiaSourceSystemOne");
        esbMessage.setMessageType("PartialEntityOne");
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }
        Criterion[] c1 = {new Criterion(SearchField.sourceSystem, "PartiaSourceSystemOne")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);
        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("<Payload><Hello> is it me you're looking for ?</Hello>"+
                "<Example>Sensitive Information is not viewable</Example>"+
                "<Example>Sensitive Information is not viewable</Example></Payload>", result.getMessages()[0].getPayload());
        Query query = getEntityManager().createQuery("select f from EsbMessageSensitiveInfoEntity f");
        List<EsbMessageSensitiveInfoEntity> queryResult = (List<EsbMessageSensitiveInfoEntity>) query.getResultList();
        Assert.assertEquals("two sensitive bits expected", 2, queryResult.size());
        EncryptionUtility util = new EncryptionUtility("passwordpassword");
        Assert.assertEquals("<Example>I can see it in your eyes</Example>",util.decrypt(queryResult.get(0).getValue()));
        Assert.assertEquals("<Example>I can see it in your soul</Example>",util.decrypt(queryResult.get(1).getValue()));
        Assert.assertEquals(result.getMessages()[0].getId(),queryResult.get(1).getEsbMessage().getId().longValue());
    }


    /*
     * TODO : fix example json regex, Does not work currently.
     */
    @Test
    public void testSegregateSensitiveInfoFromJSON() {
        EsbMessage esbMessage = createTestMessage(154, 0);
        esbMessage.setMessageType("PartialEntityTwo");
        String payload = "{\"message\": { \"id\": \"blah\", \"Example\": \"My credit card number is 1234 3121 3123 3123\" }}";
        esbMessage.setPayload(payload);
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }
        Criterion[] c2 = {new Criterion(SearchField.messageType, "PartialEntityTwo")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c2);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);
        result = service.getMessageById(result.getMessages()[0].getId());
        //Assert.assertEquals("{\"message\": { \"id\": \"blah\", \"Example\": \"I will never tell you my credit card number\" }}", result.getMessages()[0].getPayload());
    }

    /*
     * segregating sensitive info and resubmit proof of concept
     */
    @Test
    public void resubmitWithSegregatedSensitiveInfoPOC() {

      String oldText = "<Payload><Hello> is it me you're looking for ?</Hello>"+
                "<Example>I can see it in your eyes</Example>"+
              "<Example>I can see it in your soul</Example></Payload> ";
      String text = oldText;
      String parentTag = "Example", replaceText = "Sensitive Information is not viewable";

      Pattern pattern = Pattern.compile("<("+parentTag+")>((?!<("+parentTag+")>).)*</("+parentTag+")>");
      Matcher matcher = pattern.matcher(text);

      ArrayList<String> sensitiveInformation = new ArrayList<>();
      while(matcher.find()) {
          sensitiveInformation.add(matcher.group(0));
      }
      Assert.assertEquals(2, sensitiveInformation.size());
      Assert.assertEquals("<Example>I can see it in your eyes</Example>", sensitiveInformation.get(0));
      Assert.assertEquals("<Example>I can see it in your soul</Example>", sensitiveInformation.get(1));
      matcher.reset();

      text = matcher.replaceAll("<$1>"+replaceText+"</$1>");
      Assert.assertEquals("<Payload><Hello> is it me you're looking for ?</Hello>" +
              "<Example>Sensitive Information is not viewable</Example>" +
              "<Example>Sensitive Information is not viewable</Example></Payload> ", text);

      String patternString2 = "<"+parentTag+">"+replaceText+"</"+parentTag+">";
      Pattern pattern2 = Pattern.compile(patternString2);

      for(String info: sensitiveInformation) {
          text = pattern2.matcher(text).replaceFirst(info);
      }
      Assert.assertEquals( oldText, text);

    }

    @Test
    public void testFetchPayloadByMessageId() {
        EsbMessage esbMessage = createTestMessage(12, 0);
        esbMessage.setPayload("<Payload>Payload</Payload>");
        esbMessage.setSourceSystem("SourceSystem12");
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        Criterion[] c1 = {new Criterion(SearchField.sourceSystem, "SourceSystem12")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);

        Assert.assertEquals(1, result.getMessages().length);
        Assert.assertEquals("SourceSystem12",result.getMessages()[0].getSourceSystem());
        Assert.assertNull(result.getMessages()[0].getPayload());

        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("SourceSystem12",result.getMessages()[0].getSourceSystem());
        Assert.assertEquals("<Payload>Payload</Payload>",result.getMessages()[0].getPayload());
    }

    @Test
    public void testHiddenPayloadUponSingleCriteriaMatch() {
        EsbMessage esbMessage = createTestMessage(14, 0);
        esbMessage.setPayload("<Payload>NotHiddenPayload</Payload>");
        esbMessage.setSourceSystem("SourceSystemTwo");
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }

        SearchCriteria criteria = new SearchCriteria();
        Criterion[] c1 = {new Criterion(SearchField.sourceSystem, "SourceSystemTwo")};
        criteria.setCriteria(c1);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);

        // if the match criterion has just one condition, and the message satisfies it, payload should be hidden
        Assert.assertEquals(1, result.getMessages().length);
        Assert.assertEquals("SourceSystemTwo",result.getMessages()[0].getSourceSystem());
        Assert.assertNull(result.getMessages()[0].getPayload());

        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("SourceSystemTwo",result.getMessages()[0].getSourceSystem());
        Assert.assertEquals("SourceSystemTwo messages are restricted",result.getMessages()[0].getPayload());

    }


    @Test
    public void testPayloadNotHiddenUponPartialCriteriaMatch() {
        EsbMessage esbMessage = createTestMessage(14, 0);
        esbMessage.setPayload("<Payload>NotHiddenPayload</Payload>");
        esbMessage.setSourceSystem("SourceSystemOne");
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }
        Criterion[] c2 = {new Criterion(SearchField.sourceSystem, "SourceSystemOne")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c2);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);
        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("<Payload>NotHiddenPayload</Payload>",result.getMessages()[0].getPayload());
    }


    @Test
    public void testHiddenPayloadUponMultipleCriteriaMatch() {
        EsbMessage esbMessage = createTestMessage(14, 0);
        esbMessage.setPayload("<Payload>NotHiddenPayload</Payload>");
        esbMessage.setSourceSystem("SourceSystemOne");
        esbMessage.setMessageType("EntityOne");
        // if the match criterion has more than one condition, and the message satisfies ALL of the conditions, payload should be hidden
        try {
            service.persist(esbMessage);
        } catch (IOException e) {
            Assert.fail();
        }
        Criterion[] c3 = {new Criterion(SearchField.sourceSystem, "SourceSystemOne"),new Criterion(SearchField.messageType, "EntityOne")};
        SearchCriteria criteria = new SearchCriteria();
        criteria.setCriteria(c3);
        SearchResult result = service.searchMessagesByCriteria(criteria, null, null, "sourceSystem", false, 0, 10);
        result = service.getMessageById(result.getMessages()[0].getId());
        Assert.assertEquals("EntityOne messages from SourceSystemOne are restricted",result.getMessages()[0].getPayload());
    }

    @Test
    public void testSortingResultsOnCriteriaSearch() {
        EsbMessage esbMessage0 = createTestMessage(9, 0);
        EsbMessage esbMessage1 = createTestMessage(9, 0);
        EsbMessage esbMessage2 = createTestMessage(9, 0);
        EsbMessage esbMessage3 = createTestMessage(9, 0);
        esbMessage0.setSourceSystem("SourceSystem0");
        esbMessage1.setSourceSystem("SourceSystem1");
        esbMessage2.setSourceSystem("SourceSystem2");
        esbMessage3.setSourceSystem("SourceSystem3");

        // persist the messages
        try {
            service.persist(esbMessage3);
            service.persist(esbMessage2);
            service.persist(esbMessage1);
            service.persist(esbMessage0);
        } catch (IOException e) {
            Assert.fail();
        }

         Criterion[] c1 = {new Criterion(SearchField.messageGuid, "MessageGuid9")};
         SearchCriteria criteria1 = new SearchCriteria();
         criteria1.setCriteria(c1);
         SearchResult result = service.searchMessagesByCriteria(criteria1, null, null, "sourceSystem", true, 0, 10);

         Assert.assertEquals("SourceSystem0", result.getMessages()[0].getSourceSystem());
         Assert.assertEquals("SourceSystem1", result.getMessages()[1].getSourceSystem());
         Assert.assertEquals("SourceSystem2", result.getMessages()[2].getSourceSystem());
         Assert.assertEquals("SourceSystem3", result.getMessages()[3].getSourceSystem());

         result = service.searchMessagesByCriteria(criteria1, null, null, "sourceSystem", false, 0, 10);

         Assert.assertEquals("SourceSystem0", result.getMessages()[3].getSourceSystem());
         Assert.assertEquals("SourceSystem1", result.getMessages()[2].getSourceSystem());
         Assert.assertEquals("SourceSystem2", result.getMessages()[1].getSourceSystem());
         Assert.assertEquals("SourceSystem3", result.getMessages()[0].getSourceSystem());
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

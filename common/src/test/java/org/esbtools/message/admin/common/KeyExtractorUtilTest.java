package org.esbtools.message.admin.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.esbtools.message.admin.common.extractor.KeyExtractorException;
import org.esbtools.message.admin.common.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataType;
import org.junit.Test;

/**
 * Unit test to demonstrate using the KeyExtractor Util
 *
 * @author ykoer
 */

public class KeyExtractorUtilTest {

    @Test
    public void testKeyExtractorGood() {

        String payload =
                "<Person>\n" +
                "    <Id>12345</Id>\n" +
                "    <References>\n" +
                "        <Id system=\"SystemA\">4532</Id>\n" +
                "        <Id system=\"SystemB\">1532</Id>\n" +
                "    </References>\n" +
                "    <FirstName>John</FirstName>\n" +
                "    <LastName>Doe</LastName>\n" +
                "    <Email confirmed=\"true\">john.doe@example.com</Email>\n" +
                "    <Addresses>\n" +
                "        <Address type=\"Private\">\n" +
                "            <Line>123 Main St</Line>\n" +
                "            <City>Anytown</City>\n" +
                "            <State>AS</State>\n" +
                "            <Country>US</Country>\n" +
                "            <ZIP>98765</ZIP>\n" +
                "        </Address>\n" +
                "        <Address type=\"Work\">\n" +
                "            <Line>456 Business St</Line>\n" +
                "            <City>Anytown</City>\n" +
                "            <State>AS</State>\n" +
                "            <Country>US</Country>\n" +
                "            <ZIP>98765</ZIP>\n" +
                "        </Address>\n" +
                "    </Addresses>\n" +
                "</Person>";

        List<MetadataField> extractors = new ArrayList<MetadataField>();

        MetadataField searchKey1 = new MetadataField(MetadataType.SearchKey, "The confirmed email address", "email");
        MetadataField field1 = new MetadataField(MetadataType.XPATH, "", "/Person/Email[@confirmed='true']");
        searchKey1.addDescendant(field1);
        extractors.add(searchKey1);

        MetadataField searchKey2 = new MetadataField(MetadataType.SearchKey, "The System B Identifier", "systemBId");
        MetadataField field2 = new MetadataField(MetadataType.XPATH, "", "/Person/References/Id[@system='SystemB']");
        searchKey2.addDescendant(field2);
        extractors.add(searchKey2);

        MetadataField searchKey3 = new MetadataField(MetadataType.SearchKey, "Is email is confirmed?", "emailConfirmed");
        MetadataField field3 = new MetadataField(MetadataType.XPATH, "", "/Person/Email/@confirmed");
        searchKey3.addDescendant(field3);
        extractors.add(searchKey3);

        MetadataField searchKey4 = new MetadataField(MetadataType.SearchKey, "The address type", "addressType");
        MetadataField field4 = new MetadataField(MetadataType.XPATH, "", "/Person/Addresses/Address/@type");
        searchKey4.addDescendant(field4);
        extractors.add(searchKey4);

        MetadataField searchKey5 = new MetadataField(MetadataType.SearchKey, "Number of addresses", "addressCount");
        MetadataField field5 = new MetadataField(MetadataType.XPATH, "", "count(/Person/Addresses/Address)");
        searchKey5.addDescendant(field5);
        extractors.add(searchKey5);

        MetadataField searchKey6 = new MetadataField(MetadataType.SearchKey, "Person name", "name");
        MetadataField field6 = new MetadataField(MetadataType.XPATH, "", "concat(/Person/FirstName,' ',/Person/LastName)");
        searchKey6.addDescendant(field6);
        extractors.add(searchKey6);

        try {
            KeyExtractorUtil util = new KeyExtractorUtil(extractors);
            Map<String,List<String>> extractedData = util.getEntriesFromPayload(payload);

            assertEquals(1, extractedData.get("email").size());
            assertEquals("john.doe@example.com", extractedData.get("email").get(0));
            assertEquals(1, extractedData.get("systemBId").size());
            assertEquals("1532", extractedData.get("systemBId").get(0));
            assertEquals(1, extractedData.get("emailConfirmed").size());
            assertEquals("true", extractedData.get("emailConfirmed").get(0));
            assertEquals(2, extractedData.get("addressType").size());
            assertEquals("Private", extractedData.get("addressType").get(0));
            assertEquals("Work", extractedData.get("addressType").get(1));
            assertEquals(1, extractedData.get("addressCount").size());
            assertEquals("2", extractedData.get("addressCount").get(0));
            assertEquals(1, extractedData.get("name").size());
            assertEquals("John Doe", extractedData.get("name").get(0));
        } catch (KeyExtractorException e) {
            System.out.println(e);
            fail();
        }
    }

    @Test
    public void testKeyExtractorBadPayload() {

        String badPayload =
                "<Person>\n" +
                "    <Id>12345</Id>\n" +
                "    <References>\n" +
                "        <Id system=\"SystemA\">4532</Id>\n" +
                "        <Id system=\"SystemB\">1532</Id>\n" +
                "    </References>\n" +
                "    <FirstName>John</FirstName>\n" +
                "    <LastName>Doe</LastName>\n" +
                "    <Email confirmed=\"true\">john.doe@example.com</Email>\n" +
                "    <Addresses>\n" +
                "        <Address type=\"Private\">\n" +
                "            <Line>123 Main St</Line>\n" +
                "            <City>Anytown</City>\n" +
                "            <State>AS</State>\n" +
                "            <Country>US</Country>\n" +
                "            <ZIP>98765</ZIP>\n" +
                "        </Address>\n" +
                "        <Address type=\"Work\">\n" +
                "            <Line>456 Business St</Line>\n" +
                "            <City>Anytown</City>\n" +
                "            <State>AS</State>\n" +
                "            <Country>US</Country>\n" +
                "            <ZIP>98765</ZIP>\n" +
                "        </Address>\n" +
                "    </Addresses>\n" +
                "<Person>"; // XML is not well-formed. A closing Person element is expected

        List<MetadataField> extractors = new ArrayList<MetadataField>();

        MetadataField searchKey1 = new MetadataField(MetadataType.SearchKey, "lastName", "Persons last name");
        MetadataField field1 = new MetadataField(MetadataType.XPATH, "", "/Person/LastName");
        searchKey1.addDescendant(field1);
        extractors.add(searchKey1);

        try {
            KeyExtractorUtil util = new KeyExtractorUtil(extractors);
            Map<String,List<String>> extractedData = util.getEntriesFromPayload(badPayload);
            fail();

        } catch (KeyExtractorException e) {
            assertEquals("XML document structures must start and end within the same entity.", e.getCause().getMessage());
        }

    }

    @Test
    public void testKeyExtractorBadXpath() {

        String payload =
                "<Person>\n" +
                "    <Id>12345</Id>\n" +
                "    <References>\n" +
                "        <Id system=\"SystemA\">4532</Id>\n" +
                "        <Id system=\"SystemB\">1532</Id>\n" +
                "    </References>\n" +
                "    <FirstName>John</FirstName>\n" +
                "    <LastName>Doe</LastName>\n" +
                "    <Email confirmed=\"true\">john.doe@example.com</Email>\n" +
                "</Person>";

        List<MetadataField> extractors = new ArrayList<MetadataField>();

        MetadataField searchKey1 = new MetadataField(MetadataType.SearchKey, "name", "Person name");
        MetadataField field1 = new MetadataField(MetadataType.XPATH, "", "concat((/Person/FirstName,' ',/Person/LastName)");
        searchKey1.addDescendant(field1);
        extractors.add(searchKey1);

        try {
            KeyExtractorUtil util = new KeyExtractorUtil(extractors);
            Map<String,List<String>> extractedData = util.getEntriesFromPayload(payload);

            // invalid xpath are ignored.
            assertEquals(0, extractedData.size());

        } catch (KeyExtractorException e) {
            fail();
        }

    }
}

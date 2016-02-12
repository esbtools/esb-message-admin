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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
    public void testKeyHappyPathExtraction() {

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

        MetadataField searchKey7 = new MetadataField(MetadataType.SearchKey, "Id", "id");
        MetadataField field7 = new MetadataField(MetadataType.XPATH, "", "/Person/Id/text()");
        searchKey7.addDescendant(field7);
        extractors.add(searchKey7);

        try {
            KeyExtractorUtil util = new KeyExtractorUtil(extractors,"test");
            Map<String,Set<String>> extractedData = util.getEntriesFromPayload(payload);

            assertEquals(1, extractedData.get("email").size());
            assertThat(extractedData.get("email"), hasItem("john.doe@example.com"));
            assertEquals(1, extractedData.get("systemBId").size());
            assertThat(extractedData.get("systemBId"), hasItem("1532"));
            assertEquals(1, extractedData.get("emailConfirmed").size());
            assertThat(extractedData.get("emailConfirmed"), hasItem("true"));
            assertEquals(2, extractedData.get("addressType").size());
            assertThat(extractedData.get("addressType"), hasItems("Private", "Work"));
            assertEquals(1, extractedData.get("addressCount").size());
            assertThat(extractedData.get("addressCount"), hasItem("2"));
            assertEquals(1, extractedData.get("name").size());
            assertThat(extractedData.get("name"), hasItem("John Doe"));
            assertEquals(1, extractedData.get("id").size());
            assertThat(extractedData.get("id"), hasItem("12345"));
        } catch (KeyExtractorException e) {
            System.out.println(e);
            fail();
        }
    }

    @Test
    public void testKeyExtractorExceptionWithMalformedPayload() {

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
            KeyExtractorUtil util = new KeyExtractorUtil(extractors,"test");
            Map<String,Set<String>> extractedData = util.getEntriesFromPayload(badPayload);
            fail();

        } catch (KeyExtractorException e) {
            assertEquals("XML document structures must start and end within the same entity.", e.getCause().getMessage());
        }

    }

    @Test
    public void testKeyExtractorWithMalformedXpath() {

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
            KeyExtractorUtil util = new KeyExtractorUtil(extractors,"test");
            Map<String,Set<String>> extractedData = util.getEntriesFromPayload(payload);

            // invalid xpath are ignored.
            assertEquals(0, extractedData.size());

        } catch (KeyExtractorException e) {
            fail();
        }

    }
}


package org.esbtools.message.admin.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SearchFieldTest {

    @Test
    public void testGetValueTypeTrue() {
        assertEquals(SearchField.valueOf("messageType").getValueType(), String.class);
    }

    @Test
    public void testNotPreDefined() {
        assertFalse(SearchField.isPreDefined("customfield"));
    }
    
    @Test
    public void testIsPreDefined() {
        assertTrue(SearchField.isPreDefined("id"));
        assertTrue(SearchField.isPreDefined("errorQueue"));
        assertTrue(SearchField.isPreDefined("messageId"));
        assertTrue(SearchField.isPreDefined("messageGuid"));
        assertTrue(SearchField.isPreDefined("messageType"));
        assertTrue(SearchField.isPreDefined("sourceQueue"));
        assertTrue(SearchField.isPreDefined("sourceSystem"));
        assertTrue(SearchField.isPreDefined("originalSystem"));
        assertTrue(SearchField.isPreDefined("queueName"));
        assertTrue(SearchField.isPreDefined("queueLocation"));
        assertTrue(SearchField.isPreDefined("errorComponent"));
        assertTrue(SearchField.isPreDefined("serviceName"));
        assertTrue(SearchField.isPreDefined("customHeader"));
    }
    
    @Test
    public void testIsPreDefinedUpperCase() {
        assertTrue(SearchField.isPreDefined("ID"));
        assertTrue(SearchField.isPreDefined("ERRORQUEUE"));
        assertTrue(SearchField.isPreDefined("MESSAGEID"));
        assertTrue(SearchField.isPreDefined("MESSAGEGUID"));
        assertTrue(SearchField.isPreDefined("MESSAGETYPE"));
        assertTrue(SearchField.isPreDefined("SOURCEQUEUE"));
        assertTrue(SearchField.isPreDefined("SOURCESYSTEM"));
        assertTrue(SearchField.isPreDefined("ORIGINALSYSTEM"));
        assertTrue(SearchField.isPreDefined("QUEUENAME"));
        assertTrue(SearchField.isPreDefined("QUEUELOCATION"));
        assertTrue(SearchField.isPreDefined("ERRORCOMPONENT"));
        assertTrue(SearchField.isPreDefined("SERVICENAME"));
        assertTrue(SearchField.isPreDefined("CUSTOMHEADER"));
    }

    @Test
    public void testIsPreDefinedLowerCase() {
        assertTrue(SearchField.isPreDefined("id"));
        assertTrue(SearchField.isPreDefined("errorqueue"));
        assertTrue(SearchField.isPreDefined("messageid"));
        assertTrue(SearchField.isPreDefined("messageguid"));
        assertTrue(SearchField.isPreDefined("messagetype"));
        assertTrue(SearchField.isPreDefined("sourcequeue"));
        assertTrue(SearchField.isPreDefined("sourcesystem"));
        assertTrue(SearchField.isPreDefined("originalsystem"));
        assertTrue(SearchField.isPreDefined("queuename"));
        assertTrue(SearchField.isPreDefined("queuelocation"));
        assertTrue(SearchField.isPreDefined("errorcomponent"));
        assertTrue(SearchField.isPreDefined("servicename"));
        assertTrue(SearchField.isPreDefined("customheader"));
    }
    
    @Test
    public void testFind() {
        List<String> expectedResults = Arrays.asList("id","messageId", "messageGuid");
        List<String> findResults = SearchField.find("id");
        assertEquals(expectedResults, findResults);
        //fail("Not yet implemented");
    }

}

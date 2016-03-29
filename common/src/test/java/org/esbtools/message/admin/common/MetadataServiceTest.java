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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.esbtools.message.admin.common.utility.ConversionUtility;
import org.esbtools.message.admin.common.orm.MetadataEntity;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.Header;
import org.esbtools.message.admin.model.HeaderType;
import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.junit.Assert;
import org.junit.Test;

/**
 * ESBMessageAdmin Service Unit Tests
 * @author ykoer
 *
 */
public class MetadataServiceTest extends EsbMessageAdminTestBase {

    @Test
    public void testAddingChildMetadataFields() {
        long id = -1L;
        service.addChildMetadataField(id, "Entities", MetadataType.Entities, "Entities");

        MetadataResponse result = service.getMetadataTree(MetadataType.Entities);
        assertSuccess(result);
        MetadataField field = fetchMetadataField("Entities", "Entities", MetadataType.Entities);
        for (int i = 1; i < 10; i++) {
            result = service.addChildMetadataField(field.getId().longValue(), "Entity" + i, MetadataType.Entity, "entity" + i);
            assertSuccess(result);
            Assert.assertEquals(result.getTree().getId().longValue(), field.getId().longValue());
            Assert.assertEquals(result.getTree().getChildren().size(), i);
            Assert.assertEquals(result.getResult().getId().longValue(), field.getId().longValue());
            MetadataField child = fetchMetadataField("Entity" + i, "entity" + i, MetadataType.Entity);
            assertfieldAssertions("Entity" + i, "entity" + i, MetadataType.Entity, child);

        }
    }

    @Test
    public void testAddingMetadataFieldWithIncorrectType() {
        long id = -1L;
        service.addChildMetadataField(id, "Entities", MetadataType.Entities, "Entities");

        MetadataResponse result = service.getMetadataTree(MetadataType.Entities);
        assertSuccess(result);
        MetadataField field = fetchMetadataField("Entities", "Entities", MetadataType.Entities);
        result = service.addChildMetadataField(field.getId().longValue(), "System", MetadataType.System, "system");
        assertError(result, "Illegal Argument:System can not be a child of Entities");
    }

    @Test
    public void testAddingFieldToMissingParent() {
        MetadataResponse result = service.addChildMetadataField(40L, "System", MetadataType.System, "system");
        assertError(result, "Illegal Argument:parent 40 not found!");
    }

    @Test
    public void testAddingIllegalRootField() {
        MetadataResponse result = service.addChildMetadataField(-1L, "System", MetadataType.System, "system");
        assertError(result, "Illegal Argument:" + MetadataType.System + ", If parent = -1, Expected: Entities or SearchKeys");
    }

    private MetadataField fetchMetadataField(String name, String value, MetadataType type) {
        MetadataField result = null;
        Query query = getEntityManager().createQuery("select f from MetadataEntity f where f.name = :name and f.type = :type and f.value = :value");
        query.setParameter("name", name);
        query.setParameter("type", type);
        query.setParameter("value", value);
        List<MetadataEntity> queryResult = (List<MetadataEntity>) query.getResultList();
        if (queryResult != null && queryResult.size() != 0) {
            result = ConversionUtility.convertToMetadataField(queryResult.get(0));
        }
        return result;
    }

    @Test
    public void testUpdatingMetadataFields() {
        long id = -1L;
        service.addChildMetadataField(id, "Entities", MetadataType.Entities, "Entities");
        MetadataField parent = fetchMetadataField("Entities", "Entities", MetadataType.Entities);
        service.addChildMetadataField(parent.getId(), "EntityPreUpdate", MetadataType.Entity, "entityPreUpdate");
        MetadataField child = fetchMetadataField("EntityPreUpdate", "entityPreUpdate", MetadataType.Entity);
        assertfieldAssertions("EntityPreUpdate", "entityPreUpdate", MetadataType.Entity, child);
        MetadataResponse result = service.updateMetadataField(child.getId(), "EntityPostUpdate", MetadataType.Entity, "entityPostUpdate");
        assertSuccess(result);
        assertfieldAssertions("Entities", "Entities", MetadataType.Entities, result.getResult());
        assertfieldAssertions("Entities", "Entities", MetadataType.Entities, result.getTree());
        child = fetchMetadataField("EntityPreUpdate", "entityPreUpdate", MetadataType.Entity);
        Assert.assertNull(child);
        child = fetchMetadataField("EntityPostUpdate", "entityPostUpdate", MetadataType.Entity);
        assertfieldAssertions("EntityPostUpdate", "entityPostUpdate", MetadataType.Entity, child);
    }

    @Test
    public void testMetadataFieldDeletion() {
        long id = -1L;
        MetadataResponse result = service.addChildMetadataField(id, "Entities", MetadataType.Entities, "entitiesDeleteTest");
        assertSuccess(result);
        MetadataField parent = fetchMetadataField("Entities", "entitiesDeleteTest", MetadataType.Entities);
        result = service.addChildMetadataField(parent.getId(), "EntityDelete", MetadataType.Entity, "entityDelete");
        assertSuccess(result);
        MetadataField child = fetchMetadataField("EntityDelete", "entityDelete", MetadataType.Entity);
        assertfieldAssertions("EntityDelete", "entityDelete", MetadataType.Entity, child);
        result = service.deleteMetadataField(child.getId());
        assertSuccess(result);
        Assert.assertEquals(result.getResult().getId().longValue(), parent.getId().longValue());
        Assert.assertNull("Child was not deleted!", fetchMetadataField("EntityDelete1", "entityDelete1", MetadataType.Entities));
    }

    @Test
    public void testResultCreationOnCrudOperations() {
        long id = -1L;
        service.addChildMetadataField(id, "Entities", MetadataType.Entities, "resultCreationTest");
        MetadataField parent = fetchMetadataField("Entities", "resultCreationTest", MetadataType.Entities);

        service.addChildMetadataField(parent.getId(), "EntityRes", MetadataType.Entity, "entityres");
        MetadataField child1 = fetchMetadataField("EntityRes", "entityres", MetadataType.Entity);
        service.addChildMetadataField(child1.getId(), "EntityResGrand", MetadataType.System, "entityresgrand");
        MetadataField gchild1 = fetchMetadataField("EntityResGrand", "entityresgrand", MetadataType.System);

        service.addChildMetadataField(parent.getId(), "EntityRes2", MetadataType.Entity, "entityres2");
        MetadataField child2 = fetchMetadataField("EntityRes2", "entityres2", MetadataType.Entity);
        service.addChildMetadataField(child2.getId(), "EntityResGrand2", MetadataType.System, "entityresgrand2");
        MetadataField gchild2 = fetchMetadataField("EntityResGrand2", "entityresgrand2", MetadataType.System);
        MetadataResponse result = service.updateMetadataField(child2.getId(), "EntityRes2", MetadataType.Entity, "modifiedentityres2");
        Assert.assertEquals(result.getResult().getChildren().get(0).getChildren().size(), 1);
        Assert.assertTrue(result.getResult().getChildren().get(0).getChildren().get(0).getName().startsWith("EntityRes"));
    }

    @Test
    public void testEntitiesMetadataTreeCreation() {
        long id = -1L;
        MetadataResponse result = service.addChildMetadataField(id, "Entities", MetadataType.Entities, "Entities");
        assertSuccess(result);
        MetadataField entities = fetchMetadataField("Entities", "Entities", MetadataType.Entities);

        for (int i = 0; i < 5; i++) {
            result = service.addChildMetadataField(entities.getId(), "Entity" + i, MetadataType.Entity, "entityTreeTest" + i);
            assertSuccess(result);
            assertfieldAssertions("Entities", "Entities", MetadataType.Entities, result.getResult());
            MetadataField entity = fetchMetadataField("Entity" + i, "entityTreeTest" + i, MetadataType.Entity);

            for (int j = 0; j < 3; j++) {
                result = service.addChildMetadataField(entity.getId(), "System" + i + j, MetadataType.System, "systemTreeTest" + i + j);
                assertSuccess(result);
                assertfieldAssertions("Entity" + i, "entityTreeTest" + i, MetadataType.Entity, result.getResult());
                MetadataField system = fetchMetadataField("System" + i + j, "systemTreeTest" + i + j, MetadataType.System);
                assertfieldAssertions("System" + i + j, "systemTreeTest" + i + j, MetadataType.System, system);

                for (int k = 0; k < 4; k++) {
                    result = service.addChildMetadataField(system.getId(), "SyncKey" + i + j + k, MetadataType.SyncKey, "syncKeyTreeTest" + i + j + k);
                    assertSuccess(result);
                    assertfieldAssertions("System" + i + j, "systemTreeTest" + i + j, MetadataType.System, result.getResult());
                    MetadataField key = fetchMetadataField("SyncKey" + i + j + k, "syncKeyTreeTest" + i + j + k, MetadataType.SyncKey);
                    assertfieldAssertions("SyncKey" + i + j + k, "syncKeyTreeTest" + i + j + k, MetadataType.SyncKey, key);

                }
            }
        }

        result = service.getMetadataTree(MetadataType.Entities);
        assertSuccess(result);
        Assert.assertNull("tree result should be null:", result.getResult());

        MetadataField root = result.getTree();
        for (int entity = 0; entity < 5; entity++) {
            MetadataField entityField = root.getChildren().get(entity);
            Assert.assertEquals(MetadataType.Entity, entityField.getType());
            Assert.assertTrue(entityField.getName().startsWith("Entity"));
            Assert.assertTrue(entityField.getValue().startsWith("entityTreeTest"));

            for (int system = 0; system < 3; system++) {
                MetadataField systemField = entityField.getChildren().get(system);
                Assert.assertEquals(MetadataType.System, systemField.getType());
                Assert.assertTrue(systemField.getName().startsWith("System"));
                Assert.assertTrue(systemField.getValue().startsWith("systemTreeTest"));

                for (int key = 0; key < 4; key++) {
                    MetadataField keyField = systemField.getChildren().get(key);
                    Assert.assertEquals(MetadataType.SyncKey, keyField.getType());
                    Assert.assertTrue(keyField.getName().startsWith("SyncKey"));
                    Assert.assertTrue(keyField.getValue().startsWith("syncKeyTreeTest"));

                }
            }

        }

    }

    @Test
    public void testChildWithMissingParentDoesNotThrowNPE() {
        MetadataEntity entity = new MetadataEntity(MetadataType.Entity, "missingParent", "missingParent", 3L);
        getEntityManager().persist(entity);
        MetadataResponse result = service.getMetadataTree(MetadataType.Entities);
    }

    private void assertSuccess(MetadataResponse result) {
        Assert.assertEquals(result.getErrorMessage(), MetadataResponse.Status.Success, result.getStatus());
        Assert.assertNotNull("Result's root cannot be null!", result.getTree());
    }

    private void assertError(MetadataResponse result, String errorMessage) {
        Assert.assertEquals(MetadataResponse.Status.Error, result.getStatus());
        Assert.assertEquals(errorMessage, result.getErrorMessage());
    }

    private void assertfieldAssertions(String name, String value, MetadataType type, MetadataField field) {
        Assert.assertEquals(value, field.getValue());
        Assert.assertEquals(name, field.getName());
        Assert.assertEquals(type, field.getType());
    }

    private EsbMessage getTestMessage() {
        String payload =
                "<Person>\n" +
                " <Id>12345</Id>\n" +
                " <SourceSystem>SystemA</SourceSystem>\n" +
                " <FirstName>John</FirstName>\n" +
                " <LastName>Doe</LastName>\n" +
                " <Email confirmed=\"true\">john.doe@example.com</Email>\n" +
                " <Addresses>\n" +
                " <Address type=\"Private\">\n" +
                " <Line>123 Main St</Line>\n" +
                " <City>Anytown</City>\n" +
                " <State>AS</State>\n" +
                " <Country>US</Country>\n" +
                " <ZIP>98765</ZIP>\n" +
                " </Address>\n" +
                " <Address type=\"Work\">\n" +
                " <Line>Strasse 123</Line>\n" +
                " <City>Stadt</City>\n" +
                " <Country>DE</Country>\n" +
                " <ZIP>12345</ZIP>\n" +
                " </Address>\n" +
                " </Addresses>\n" +
                "</Person>";

        EsbMessage message = new EsbMessage();
        message.setPayload(payload);
        return message;
    }


}

package org.esbtools.message.admin.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.esbtools.message.admin.model.audit.AuditCriterion;
import org.esbtools.message.admin.model.audit.AuditEvent;
import org.esbtools.message.admin.model.audit.AuditSearchCriteria;
import org.esbtools.message.admin.service.dao.audit.AuditEventDAO;
import org.esbtools.message.admin.service.dao.audit.AuditEventDAOImpl;
import org.esbtools.message.admin.service.orm.AuditEventEntity;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuditEventDAOTest {

    protected static EntityManagerFactory emf;
    protected EntityManager mgr;

    @BeforeClass
    public static void createEntityManagerFactory() {
        emf = Persistence.createEntityManagerFactory("EsbMessageAdminTestErrorPU");
    }

    @Before
    public void createEntityManager() {
        mgr = emf.createEntityManager();
        // Start a new transaction
        if (!mgr.getTransaction().isActive()) {
            mgr.getTransaction().begin();
        }
    }

    @Test
    public void testAuditEventCreation() {
        AuditEventDAO dao = new AuditEventDAOImpl(mgr);

        // store an audit event
        AuditEventEntity expected = new AuditEventEntity(new Date(),"ACTION1","ORDER","ORDER_NUMBER","12345","A message");
        expected = dao.save(expected);

        // the entity should have an id now
        Assert.assertNotNull(expected.getEventId());

        // find the the stored audit event
        AuditEventEntity got = dao.findById(expected.getEventId());

        Assert.assertEquals(expected.getEventId(), got.getEventId());
        Assert.assertEquals(expected.getLoggedTime(), got.getLoggedTime());
        Assert.assertEquals(expected.getAction(), got.getAction());
        Assert.assertEquals(expected.getMessageType(), got.getMessageType());
        Assert.assertEquals(expected.getKeyType(), got.getKeyType());
        Assert.assertEquals(expected.getMessageKey(), got.getMessageKey());
        Assert.assertEquals(expected.getMessage(), got.getMessage());

    }

    @Test
    public void testAuditEventRemoval() {
        AuditEventDAO dao = new AuditEventDAOImpl(mgr);

        // store an audit event
        AuditEventEntity expected = new AuditEventEntity(new Date(),"ACTION1","ORDER","ORDER_NUMBER","12345","A message");
        expected = dao.save(expected);

        // the entity should have an id now
        Assert.assertNotNull(expected.getEventId());

        dao.delete(expected);

        // find the the stored audit event and verify that it is really removed
        AuditEventEntity got = dao.findById(expected.getEventId());
        Assert.assertNull(got);
    }

    @Test
    public void testAuditEventFindAll() {
        AuditEventDAO dao = new AuditEventDAOImpl(mgr);

        // store audit events
        AuditEventEntity expected1 = new AuditEventEntity(new Date(),"ACTION1","ORDER","ORDER_NUMBER","12345","A message");
        expected1 = dao.save(expected1);
        AuditEventEntity expected2 = new AuditEventEntity(new Date(),"ACTION2","CUSTOMER","CUSTOMER_NUMBER","24221","Another message");
        expected2 = dao.save(expected2);

        List<AuditEventEntity> got = dao.findAll();
        Assert.assertNotNull(got);
        Assert.assertEquals(2, got.size());
    }

    @Test
    public void testAuditEventSearch() {
        AuditEventDAO dao = new AuditEventDAOImpl(mgr);

        dao.deleteAll();

        // store audit events
        AuditEventEntity event1 = new AuditEventEntity(new DateTime("2015-02-23T12:39:45").toDate(),"ACTION1","MESSAGE_TYPE1","KEY_TYPE1","1001","Message 1");
        dao.save(event1);
        AuditEventEntity event2 = new AuditEventEntity(new DateTime("2015-02-23T12:55:21").toDate(),"ACTION2","MESSAGE_TYPE1","KEY_TYPE2","1002","Message 2");
        dao.save(event2);
        AuditEventEntity event3 = new AuditEventEntity(new DateTime("2015-02-24T01:00:12").toDate(),"ACTION2","MESSAGE_TYPE1","KEY_TYPE4","1003","Message 3");
        dao.save(event3);
        AuditEventEntity event4 = new AuditEventEntity(new DateTime("2015-02-24T02:17:12").toDate(),"ACTION1","MESSAGE_TYPE2","KEY_TYPE4","1004","Message 4");
        dao.save(event4);
        AuditEventEntity event5 = new AuditEventEntity(new DateTime("2015-02-24T02:19:23").toDate(),"ACTION3","MESSAGE_TYPE3","KEY_TYPE2","1005","Message 5");
        dao.save(event5);

        AuditCriterion c1 = new AuditCriterion(AuditCriterion.Field.ACTION, "ACTION2");
        AuditSearchCriteria criteria = new AuditSearchCriteria(c1);

        List<AuditEvent> got = dao.search(criteria);
        Assert.assertEquals(2, got.size());

        // create time range criteria
        criteria = new AuditSearchCriteria(new AuditCriterion[] {
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_FROM, new DateTime("2015-02-24T00:00:00").toDate()),
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_TO, new DateTime("2015-02-24T03:00:00").toDate())
                });
        got = dao.search(criteria);
        Assert.assertEquals(3, got.size());

        criteria = new AuditSearchCriteria(new AuditCriterion[] {
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_FROM, new DateTime("2015-02-24T02:17:00").toDate())

                });
        got = dao.search(criteria);
        Assert.assertEquals(2, got.size());

        criteria = new AuditSearchCriteria(new AuditCriterion[] {
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_TO, new DateTime("2015-02-24T02:18:00").toDate())

                });
        got = dao.search(criteria);
        Assert.assertEquals(4, got.size());

        // create time range criteria with other criteria
        criteria = new AuditSearchCriteria(new AuditCriterion[] {
                new AuditCriterion(AuditCriterion.Field.MESSAGE_TYPE, "MESSAGE_TYPE1"),
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_FROM, new DateTime("2015-02-23T12:40:00").toDate()),
                new AuditCriterion(AuditCriterion.Field.LOGGED_DATE_TO, new DateTime("2015-02-23T13:00:00").toDate())
                });
        got = dao.search(criteria);
        Assert.assertEquals(1, got.size());
    }


    @After
    public void closeEntityManager() {
        if (mgr.getTransaction().isActive()) {
            if (mgr.getTransaction().getRollbackOnly()) {
                mgr.getTransaction().rollback();
            } else {
                mgr.getTransaction().commit();
            }
        }
        mgr.close();
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        emf.close();
    }
}

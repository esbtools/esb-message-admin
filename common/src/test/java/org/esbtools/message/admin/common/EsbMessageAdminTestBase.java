package org.esbtools.message.admin.common;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.esbtools.message.admin.common.EsbMessageAdminServiceImpl;
import org.esbtools.message.admin.common.extractor.KeyExtractorException;
import org.esbtools.message.admin.common.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.model.MetadataField;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.internal.runners.statements.Fail;

public abstract class EsbMessageAdminTestBase {

    protected static EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected EsbMessageAdminServiceImpl service;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("EsbMessageAdminTestPU");
    }

    @Before
    public void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
        // Start a new transaction
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        service = new EsbMessageAdminServiceImpl();
        service.setErrorEntityManager(entityManager);
        List<MetadataField> searchKeys = new ArrayList<MetadataField>();
        service.setKeyExtractor(new KeyExtractorUtil(searchKeys));
    }

    @After
    public void closeEntityManager() {
        if (entityManager.getTransaction().isActive()) {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
        }

        entityManager.getTransaction().begin();
        entityManager.createNativeQuery("truncate table METADATA").executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();

    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

}
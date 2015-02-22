package org.esbtools.message.admin.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.service.EsbMessageAdminServiceImpl;
import org.esbtools.message.admin.service.extractor.KeyExtractorException;
import org.esbtools.message.admin.service.extractor.KeyExtractorUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.internal.runners.statements.Fail;

public abstract class EsbMessageAdminTestBase {

    protected static EntityManagerFactory errorEmf;
    protected static EntityManagerFactory metadataEmf;
    protected EntityManager errorEm;
    protected EntityManager metadataEm;
    protected EsbMessageAdminServiceImpl service;

    @BeforeClass
    public static void createEntityManagerFactory() {
        metadataEmf = Persistence.createEntityManagerFactory("EsbMessageAdminTestMetadataPU");
        errorEmf = Persistence.createEntityManagerFactory("EsbMessageAdminTestErrorPU");
    }

    @Before
    public void createEntityManager() {
        errorEm = errorEmf.createEntityManager();
        metadataEm = metadataEmf.createEntityManager();
        // Start a new transaction
        if (!errorEm.getTransaction().isActive()) {
            errorEm.getTransaction().begin();
        }
        if (!metadataEm.getTransaction().isActive()) {
            metadataEm.getTransaction().begin();
        }
        service = new EsbMessageAdminServiceImpl();
        service.setErrorEntityManager(errorEm);
        service.setMetadataEntityManager(metadataEm);
        List<MetadataField> searchKeys = new ArrayList<MetadataField>();
        service.setKeyExtractor(new KeyExtractorUtil(searchKeys));
    }

    @After
    public void closeEntityManager() {
        if (errorEm.getTransaction().isActive()) {
            if (errorEm.getTransaction().getRollbackOnly()) {
                errorEm.getTransaction().rollback();
            } else {
                errorEm.getTransaction().commit();
            }
        }
        errorEm.close();

        // commit to make sure there are no commit errors
        if (metadataEm.getTransaction().isActive()) {
            if (metadataEm.getTransaction().getRollbackOnly()) {
                metadataEm.getTransaction().rollback();
            } else {
                metadataEm.getTransaction().commit();
            }
        }
        metadataEm.getTransaction().begin();
        metadataEm.createNativeQuery("truncate table METADATA").executeUpdate();
        metadataEm.getTransaction().commit();
        metadataEm.close();

    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        metadataEmf.close();
        errorEmf.close();
    }

    protected EntityManager getMetadataEntityManager() {
        return metadataEm;
    }

    protected EntityManager getErrorEntityManager() {
        return errorEm;
    }

}
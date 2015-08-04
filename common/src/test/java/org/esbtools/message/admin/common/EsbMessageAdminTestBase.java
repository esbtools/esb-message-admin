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
        entityManager.createNativeQuery("truncate table ESB_MESSAGE").executeUpdate();
        entityManager.createNativeQuery("truncate table ESB_MESSAGE_HEADER").executeUpdate();
        entityManager.createNativeQuery("truncate table AUDIT_EVENT").executeUpdate();
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
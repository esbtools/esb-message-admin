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
package org.esbtools.message.admin.common.dao;

import java.util.Date;

import javax.persistence.EntityManager;

import org.esbtools.message.admin.common.orm.AuditEventEntity;

/**
 * The DAO impl for audit events.
 *
 * @author vrjain
 *
 */
public class AuditEventDAOImpl implements AuditEventDAO {

    private final EntityManager mgr;

    public AuditEventDAOImpl(EntityManager mgr) {
        this.mgr=mgr;
    }

    @Override
    public void save(String principal, String action, String messageType, String keyType, String messageKey, String message) {

        AuditEventEntity event = new AuditEventEntity(new Date(), principal, action, messageType, keyType, messageKey, message);
        mgr.persist(event);
    }

}

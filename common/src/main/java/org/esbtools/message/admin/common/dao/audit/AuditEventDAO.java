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
package org.esbtools.message.admin.common.dao.audit;

import java.util.List;

import org.esbtools.message.admin.common.dao.generic.GenericDAO;
import org.esbtools.message.admin.common.orm.AuditEventEntity;
import org.esbtools.message.admin.model.audit.AuditEvent;
import org.esbtools.message.admin.model.audit.AuditSearchCriteria;

/**
 * Audit Event DAO interface which extends the generic DAO.
 *
 * @author ykoer
 *
 */
public interface AuditEventDAO extends GenericDAO<AuditEventEntity, Long> {

    /**
     * Uses the specified AuditSearchCriteria to find audit logs
     *
     * @param criteria
     *             Search criteria to find the audit logs
     * @return
     *             List of Audit Events matching the search criteria
     */
    public List<AuditEvent> search(AuditSearchCriteria criteria);

    /**
     * Uses the specified AuditSearchCriteria to count matching audit logs
     *
     * @param criteria
     *             Search criteria to find the audit logs
     * @return
     *             List of Audit Events matching the search criteria
     */
    public long count(AuditSearchCriteria criteria);

}

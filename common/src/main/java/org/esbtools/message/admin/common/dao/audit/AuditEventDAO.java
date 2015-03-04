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

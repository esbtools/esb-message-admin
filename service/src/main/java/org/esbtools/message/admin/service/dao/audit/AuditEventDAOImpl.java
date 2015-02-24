package org.esbtools.message.admin.service.dao.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.esbtools.message.admin.model.audit.AuditCriterion;
import org.esbtools.message.admin.model.audit.AuditEvent;
import org.esbtools.message.admin.model.audit.AuditSearchCriteria;
import org.esbtools.message.admin.service.dao.generic.GenericDAOImpl;
import org.esbtools.message.admin.service.orm.AuditEventEntity;

/**
 * The DAO for audit events which inherits methods from the generic DAO implementation.
 *
 * @author ykoer
 *
 */
public class AuditEventDAOImpl extends GenericDAOImpl<AuditEventEntity, Long> implements AuditEventDAO {


    public AuditEventDAOImpl(EntityManager mgr) {
        super.setEntityManager(mgr);
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.audit.AuditEventDAO#search(org.esbtools.message.admin.model.audit.AuditSearchCriteria)
     */
    public List<AuditEvent> search(AuditSearchCriteria auditCriteria) {

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<AuditEventEntity> criteriaQuery = criteriaBuilder.createQuery(AuditEventEntity.class);
        Root<AuditEventEntity> from = criteriaQuery.from(AuditEventEntity.class);

        List<AuditEventEntity> results = findItems (
                from,
                criteriaQuery,
                getPredicatesFromCriteria(auditCriteria, from),
                auditCriteria.getFirstResultIndex(),
                auditCriteria.getMaxResults(),
                auditCriteria.getSortField() != null ? auditCriteria.getSortField().toString() : null,
                auditCriteria.getDirection() != null ? auditCriteria.getDirection().toString() : null);

        return AuditEventEntity.convert(results);
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.audit.AuditEventDAO#search(org.esbtools.message.admin.model.audit.AuditSearchCriteria)
     */
    public long count(AuditSearchCriteria auditCriteria) {

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<AuditEventEntity> from = criteriaQuery.from(AuditEventEntity.class);

        return countItems (
                from,
                criteriaQuery,
                getPredicatesFromCriteria(auditCriteria, from));
    }

    private Predicate[] getPredicatesFromCriteria(AuditSearchCriteria auditCriteria, Root<AuditEventEntity> from) {

        List<Predicate> predicates = new ArrayList<Predicate>();
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();

        for (AuditCriterion c : auditCriteria.getCriteria()) {
            if (c.getField().equals(AuditCriterion.Field.LOGGED_DATE_FROM)) {
                Predicate condition = criteriaBuilder.greaterThanOrEqualTo(from.<Date> get("loggedTime"), c.getDateValue());
                predicates.add(condition);
            }
            if (c.getField().equals(AuditCriterion.Field.LOGGED_DATE_TO)) {
                Predicate condition = criteriaBuilder.lessThanOrEqualTo(from.<Date> get("loggedTime"), c.getDateValue());
                predicates.add(condition);
            }
            if (c.getField().equals(AuditCriterion.Field.ACTION)) {
                Predicate condition = criteriaBuilder.equal(from.<String> get("action"), c.getStringValue());
                predicates.add(condition);
            }
            if (c.getField().equals(AuditCriterion.Field.MESSAGE_TYPE)) {
                Predicate condition = criteriaBuilder.equal(from.<String> get("messageType"), c.getStringValue());
                predicates.add(condition);
            }
            if (c.getField().equals(AuditCriterion.Field.KEY_TYPE)) {
                Predicate condition = criteriaBuilder.equal(from.<String> get("keyType"), c.getStringValue());
                predicates.add(condition);
            }
            if (c.getField().equals(AuditCriterion.Field.MESSAGE_KEY)) {
                Predicate condition = criteriaBuilder.equal(from.<String> get("messageKey"), c.getStringValue());
                predicates.add(condition);
            }
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }
}

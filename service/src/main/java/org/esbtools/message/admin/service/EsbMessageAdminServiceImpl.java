package org.esbtools.message.admin.service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.esbtools.message.admin.model.audit.AuditEvent;
import org.esbtools.message.admin.service.dao.ConversionUtility;
import org.esbtools.message.admin.service.dao.EsbErrorDAO;
import org.esbtools.message.admin.service.dao.EsbErrorDAOImpl;
import org.esbtools.message.admin.service.dao.MetadataDAO;
import org.esbtools.message.admin.service.dao.MetadataDAOImpl;
import org.esbtools.message.admin.service.dao.audit.AuditEventDAO;
import org.esbtools.message.admin.service.dao.audit.AuditEventDAOImpl;
import org.esbtools.message.admin.service.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.service.orm.AuditEventEntity;
import org.esbtools.message.admin.spi.Provider;

@Named
public class EsbMessageAdminServiceImpl implements Provider {

    @PersistenceContext(unitName = "EsbMessageAdminPU")
    private EntityManager entityMgr;

    transient EsbErrorDAO errorDao;
    transient MetadataDAO metadataDao;
    transient AuditEventDAO auditEventDAO;
    transient static KeyExtractorUtil extractor;
    transient static boolean isMapdirty = false;

    private EsbErrorDAO getErrorDAO() {
        return errorDao == null ? new EsbErrorDAOImpl(entityMgr) : errorDao;
    }

    void setErrorEntityManager(EntityManager entityMgr) {
        this.entityMgr = entityMgr;
    }

    private MetadataDAO getMetadataDAO() {
        return metadataDao == null ? new MetadataDAOImpl(entityMgr) : metadataDao;
    }
    
    // TODO: inject DAO
    private AuditEventDAO getAuditEventDAO() {
        return auditEventDAO == null ? new AuditEventDAOImpl(entityMgr) : auditEventDAO;
    }

    private KeyExtractorUtil getKeyExtractor() {

        if (isMapdirty || extractor == null) {
            extractor = new KeyExtractorUtil(getMetadataDAO().getMetadataTree(MetadataType.SearchKeys).getTree().getChildren());
            isMapdirty = false;
        }
        return extractor;
    }

    void setKeyExtractor(KeyExtractorUtil extractor) {
        this.extractor = extractor;
    }

    @Override
    public void persist(EsbMessage esbMessage) throws IOException {
        getErrorDAO().create(ConversionUtility.convertFromEsbMessage(esbMessage), getKeyExtractor());
    }

    @Override
    public void persist(EsbMessage[] esbMessages) throws IOException {
        for (EsbMessage esbMessage:esbMessages) {
            persist(esbMessage);
        }
    }

    @Override
    public SearchResult searchMessagesByCriteria(SearchCriteria criteria, Date fromDate, Date toDate, int start, int maxResults) {

        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -30); // get this from a property file
            fromDate = c.getTime();
        }
        if (toDate == null) {
            toDate = new Date();
        }
        return getErrorDAO().findMessagesBySearchCriteria(criteria, fromDate, toDate, start, maxResults);
    }

    @Override
    public SearchResult getMessageById(Long id) {
        return getErrorDAO().getMessageById(id);
    }

    @Override
    public Map<String, List<String>> getSearchKeyValueSuggestions() {
        return getMetadataDAO().getSearchKeyValueSuggestions();
    }

    @Override
    public MetadataResponse getMetadataTree(MetadataType type) {
        return getMetadataDAO().getMetadataTree(type);
    }

    @Override
    public MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value) {
        if (type.isSearchKeyType())
            isMapdirty = true;
        return getMetadataDAO().addChildMetadataField(parentId, name, type, value);
    }

    @Override
    public MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value) {
        if (type.isSearchKeyType())
            isMapdirty = true;
        return getMetadataDAO().updateMetadataField(id, name, type, value);
    }

    @Override
    public MetadataResponse deleteMetadataField(Long id) {
        MetadataResponse response = getMetadataDAO().deleteMetadataField(id);
        if (response.getTree().getType().isSearchKeyType())
            isMapdirty = true;
        return response;
    }

    @Override
    public void sync(String entity, String system, String key, String... values) {
        // TODO Auto-generated method stub

    }

    // ------------------ Audit methods ---------------------------

    public void audit(AuditEvent event) {
    	getAuditEventDAO().save(AuditEventEntity.convert(event));
    }
}

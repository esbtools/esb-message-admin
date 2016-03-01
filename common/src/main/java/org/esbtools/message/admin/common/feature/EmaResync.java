package org.esbtools.message.admin.common.feature;

import static org.esbtools.message.admin.common.config.EMAConfiguration.getResyncRestEndpoints;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.esbtools.gateway.resync.ResyncRequest;
import org.esbtools.message.admin.common.orm.AuditEventEntity;
import org.esbtools.message.admin.common.utility.RestRequestUtility;
import org.esbtools.message.admin.model.AuditEvent;
import org.esbtools.message.admin.model.MetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class EmaResync {
    private static final Logger LOG = LoggerFactory.getLogger(EmaResync.class);
    private static final String DEFAULT_USER = "someUser";
    private static final String METADATA_KEY_TYPE = "metadata";
    
    @Inject
    private EntityManager entityMgr;
    
    public MetadataResponse syncMessage(String entity, String system, String key, String... values) {
        ResyncRequest resyncMessage = new ResyncRequest();
        resyncMessage.setEntity(entity);
        resyncMessage.setSystem(system);
        resyncMessage.setKey(key);
        resyncMessage.setValues(Arrays.asList(values));
        
        LOG.info("Initiating sync request: {}", resyncMessage.toJson());
    
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "SYNC", METADATA_KEY_TYPE, entity, key, resyncMessage.toJson()));
    
        if( RestRequestUtility.sendMessageToRestEndPoint( resyncMessage.toJson(), getResyncRestEndpoints() ) ){
            return new MetadataResponse();
        }else{
            MetadataResponse result = new MetadataResponse();
            result.setErrorMessage("Unable to resync message");
            return result;
        }
    }
    
    /**
     * Saves audit event
     * @param auditEvent
     */
    public void saveAuditEvent(AuditEvent auditEvent) {
        AuditEventEntity event = new AuditEventEntity(auditEvent);
        entityMgr.persist(event);
    }

}

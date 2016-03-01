package org.esbtools.message.admin.common.feature;

import static org.esbtools.message.admin.common.config.EMAConfiguration.getResyncRestEndpoints;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

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
        String resyncMessage = createResyncMessage(entity, system, key, values);
        
        LOG.info("Initiating sync request: {}", resyncMessage);
    
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "SYNC", METADATA_KEY_TYPE, entity, key, resyncMessage));
    
        if( RestRequestUtility.sendMessageToRestEndPoint( resyncMessage, getResyncRestEndpoints() ) ){
            return new MetadataResponse();
        }else{
            MetadataResponse result = new MetadataResponse();
            result.setErrorMessage("Unable to resync message");
            return result;
        }
    }
    
    private static String createResyncMessage(String entity, String system, String key, String... values){
        StringBuilder message = new StringBuilder("{");
        message.append("\"entity\" : \"");
        message.append(entity);
        message.append("\",");
        message.append("\"system\" : \"");
        message.append(system);
        message.append("\",");
        message.append("\"key\": \"");
        message.append(key);
        message.append("\",");
        message.append("\"values\" : [");

        int i = 0;
        for(String value: values) {
            if(value!=null && value.length()>0) {
                if(i>0) {
                    message.append(",");
                }
                message.append("\"");
                message.append(value);
                message.append("\"");
            }
            i++;
        }
        message.append("]");
        message.append("}");
        
        return message.toString();
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

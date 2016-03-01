package org.esbtools.message.admin.common.feature;

import static org.esbtools.message.admin.common.config.EMAConfiguration.getEditableMessageTypes;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getResubmitControlHeader;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getResubmitHeaderNamespace;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getResubmitRestEndpoints;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.esbtools.gateway.resubmit.ResubmitRequest;
import org.esbtools.message.admin.common.orm.AuditEventEntity;
import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.common.orm.EsbMessageHeaderEntity;
import org.esbtools.message.admin.common.utility.RestRequestUtility;
import org.esbtools.message.admin.model.AuditEvent;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.MetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class EmaResubmit {
    private static final Logger LOG = LoggerFactory.getLogger(EmaResubmit.class);
    private static final String RESUBMIT_EVENT = "Resubmit Happened";
    private static final String DEFAULT_USER = "someUser";
    
    @Inject
    private EntityManager entityMgr;
    
    public EmaResubmit(){
        
    }
    
    public MetadataResponse resubmitMessage(EsbMessage esbMessage) {

        ResubmitRequest request = new ResubmitRequest();

        EsbMessageEntity persistedMessage = entityMgr.find(EsbMessageEntity.class, esbMessage.getId());
        // scold the user for trying to update instead of insert
        if( persistedMessage == null ){
            throw new IllegalArgumentException("Message {\n}"+esbMessage.toString()+"\n} does not exist in backend store, cannot update.");
        }


        // It may be the case that the header we're configured to use isn't
        // present on the message So we should indicate this with an appropriate
        // return object so the front end can act.
        try{
            request.setDestination( persistedMessage.getHeader( getResubmitControlHeader() ).getValue() );
        }catch(Exception e){
            LOG.warn("Warning: Message that was resubmitted lacked configured control header! Message ID: " + new Long(esbMessage.getId()).toString() );
            MetadataResponse result = new MetadataResponse();
            result.setErrorMessage("Unable to resubmit message due to configured resubmit control header not being present on message.");
            return result;
        }
        request.setHeaders( reduceToEsbHeaders(persistedMessage) );
        request.setSystem( persistedMessage.getSourceSystem() );

        // explicitly check if the loaded message is in our list of allowed message types
        LOG.warn(esbMessage.toString());
        if( isEditableMessage(persistedMessage) ){
            request.setPayload( esbMessage.getPayload() );
        } else {
            request.setPayload( persistedMessage.getPayload() );
        }

        saveAuditEvent( new AuditEvent(DEFAULT_USER, RESUBMIT_EVENT, "", "", "", request.getPayload().toString() ) );
        MetadataResponse result = sendMessageToResubmitGateway( request.toString() );
        persistedMessage.setResubmittedOn(new Date());
        entityMgr.flush();
        return result;
    }
    
    /**
     * Saves audit event
     * @param auditEvent
     */
    public void saveAuditEvent(AuditEvent auditEvent) {
        AuditEventEntity event = new AuditEventEntity(auditEvent);
        entityMgr.persist(event);
    }
    
    private MetadataResponse sendMessageToResubmitGateway( String message ) {

        if( RestRequestUtility.sendMessageToRestEndPoint( message, getResubmitRestEndpoints() ) ){
            return new MetadataResponse();
        } else{
            MetadataResponse result = new MetadataResponse();
            result.setErrorMessage("Unable to resubmit message.");
            return result;
        }

    }
    
    private Boolean isEditableMessage( EsbMessageEntity message ){
        if(message.getMessageType() == null){
            return false;
        }
        
        return getEditableMessageTypes().contains( message.getMessageType().toUpperCase() );
    }
    
    private Map<String,String> reduceToEsbHeaders( EsbMessageEntity message ){
        Map<String,String> headers = new HashMap<String,String>();

        for( EsbMessageHeaderEntity header : message.getErrorHeaders() ){
            if( header.getName().contains( getResubmitHeaderNamespace() ) ){
                headers.put( header.getName(), header.getValue() );
            }
        }

        return headers;
    }
}

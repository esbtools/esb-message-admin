package org.esbtools.message.admin.model.audit;

import java.util.Date;

/**
 * An autit event model object.
 *
 * @author ykoer
 *
 */
public class AuditEvent {

    private Date loggedTime;
    private String action;
    private String messageType;
    private String keyType;
    private String messageKey;
    private String message;

    public Date getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(Date loggedTime) {
        this.loggedTime = loggedTime;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package org.esbtools.message.admin.common.orm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.esbtools.message.admin.model.audit.AuditEvent;

@Entity
@Table(name="AUDIT_EVENT")
public class AuditEventEntity {

    // ~ Instance fields
    // --------------------------------------------------------

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="event_id")
    private Long eventId;

    @Column(name = "timestamp", nullable = false)
    private Date loggedTime;

    @Column(name = "principal", nullable = false)
    private String principal;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "message_type", nullable = false)
    private String messageType;

    @Column(name = "key_type")
    private String keyType;

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Column(name = "message")
    private String message;

    // ~ Constructors
    // -----------------------------------------------------------

    public AuditEventEntity() {
    }

    public AuditEventEntity(Date loggedTime, String principal, String action, String messageType,
            String keyType, String messageKey, String message) {
        super();
        this.loggedTime = loggedTime;
        this.principal = principal;
        this.action = action;
        this.messageType = messageType;
        this.keyType = keyType;
        this.messageKey = messageKey;
        this.message = message;
    }

    // ~ Setters and Getters
    // -----------------------------------------------------------

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Date getLoggedTime() {
        return loggedTime;
    }

    public void setLoggedTime(Date loggedTime) {
        this.loggedTime = loggedTime;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
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

    // ~ Conversion helper methods
    // ----------------------------------------------------------------

    public static AuditEventEntity convert(AuditEvent event) {
        AuditEventEntity e = new AuditEventEntity();
        e.setLoggedTime(event.getLoggedTime());
        e.setPrincipal(event.getPrincipal());
        e.setAction(event.getAction());
        e.setMessageType(event.getMessageType());
        e.setKeyType(event.getKeyType());
        e.setMessageKey(event.getMessageKey());
        e.setMessage(event.getMessage());
        return e;
    }

    public static AuditEvent convert(AuditEventEntity entity) {
        AuditEvent event = new AuditEvent();
        event.setLoggedTime(entity.getLoggedTime());
        event.setPrincipal(entity.getPrincipal());
        event.setAction(entity.getAction());
        event.setMessageType(entity.getMessageType());
        event.setKeyType(entity.getKeyType());
        event.setMessageKey(entity.getMessageKey());
        event.setMessage(entity.getMessage());
        return event;
    }

    public static List<AuditEvent> convert(List<AuditEventEntity> entities) {
        List<AuditEvent> result = new ArrayList<AuditEvent>(entities.size());
        for (AuditEventEntity entity : entities) {
            result.add(convert(entity));
        }
        return result;
    }
}

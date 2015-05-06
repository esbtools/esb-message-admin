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
package org.esbtools.message.admin.common.orm;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

}

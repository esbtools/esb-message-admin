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
package org.esbtools.message.admin.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class EsbMessage implements Serializable {

    private static final long serialVersionUID = -6112339731962679492L;

    public enum ErrorType {
        DATA_ERROR, SYSTEM_ERROR
    }

    private long id;
    private String errorQueue;
    private String messageId;
    private Date timestamp;
    private Date resubmittedOn;
    private String messageGuid;
    private String messageType;
    private String sourceQueue;
    private String sourceLocation;
    private String sourceSystem;
    private String originalSystem;
    private String serviceName;
    private String errorComponent;
    private String errorMessage;
    private String errorDetails;
    private String errorSystem;
    private ErrorType errorType;
    private int occurrenceCount;
    private String payload;
    private List<Header> headers;
    private Boolean editableMessage;
    private Boolean allowsResumbit;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getErrorQueue() {
        return errorQueue;
    }

    public void setErrorQueue(String errorQueue) {
        this.errorQueue = errorQueue;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getResubmittedOn() {
        return this.resubmittedOn;
    }

    public void setResubmittedOn(Date resubmittedOn) {
        this.resubmittedOn = resubmittedOn;
    }

    public String getMessageGuid() {
        return messageGuid;
    }

    public void setMessageGuid(String messageGuid) {
        this.messageGuid = messageGuid;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSourceQueue() {
        return sourceQueue;
    }

    public void setSourceQueue(String sourceQueue) {
        this.sourceQueue = sourceQueue;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getOriginalSystem() {
        return originalSystem;
    }

    public void setOriginalSystem(String originalSystem) {
        this.originalSystem = originalSystem;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getErrorComponent() {
        return errorComponent;
    }

    public void setErrorComponent(String errorComponent) {
        this.errorComponent = errorComponent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    /**
     * @return the errorSystem
     */
    public String getErrorSystem() {
        return errorSystem;
    }

    /**
     * @param errorSystem the errorSystem to set
     */
    public void setErrorSystem(String errorSystem) {
        this.errorSystem = errorSystem;
    }

    /**
     * @return the errorType
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * @param errorType the errorType to set
     */
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * @return the occurrenceCount
     */
    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    /**
     * @param occurrenceCount the occurrenceCount to set
     */
    public void setOccurrenceCount(int occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public Boolean isEditableMessage() {
        return this.editableMessage;
    }

    public void setEditableMessage(Boolean isEditableMessage) {
        this.editableMessage = editableMessage;
    }

    public Boolean isAllowsResubmit() {
        return this.allowsResumbit;
    }

    public void setAllowsResubmit(Boolean allowsResumbit) {
        this.allowsResumbit = allowsResumbit;
    }

    @Override
    public String toString() {
        return "EsbMessage [id=" + id
                + ", errorQueue=" + errorQueue
                + ", messageId=" + messageId
                + ", timestamp=" + timestamp
                + ", messageGuid=" + messageGuid
                + ", sourceQueue=" + sourceQueue
                + ", sourceLocation=" + sourceLocation
                + ", messageType="+ messageType
                + ", originalSystem=" + originalSystem
                + ", sourceSystem=" + sourceSystem
                + ", serviceName="  + serviceName
                + ", errorComponent=" + errorComponent
                + ", errorMessage=" + errorMessage
                + ", errorDetails=" + errorDetails
                + ", errorSystem=" + errorSystem
                + ", errorType=" + errorType
                + ", occurrenceCount=" + occurrenceCount
                + ", payload=" + payload
                + ", headers=" + headers + "]";
    }
}

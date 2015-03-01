package org.esbtools.message.admin.common.orm;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.esbtools.message.admin.model.EsbMessage.ErrorType;

@Entity
@Table(name="ESB_MESSAGE")
public class EsbMessageEntity implements Serializable {

    private static final long serialVersionUID = 357984147079041238L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="id")
    private Long id;

    @Column(name="error_queue")
    private String errorQueue;

    @Column(name = "jms_message_id")
    private String messageId;

    @Column(name = "jms_message_timestamp")
    private Date timestamp;

    @Column(name="message_guid")
    private String messageGuid;

    @Column(name = "message_type")
    private String messageType;

    @Column(name="source_queue")
    private String sourceQueue;

    @Column(name="source_location")
    private String sourceLocation;

    @Column(name="source_system")
    private String sourceSystem;

    @Column(name="service_name")
    private String serviceName;

    @Column(name="error_component")
    private String errorComponent;

    @Column(name="error_message")
    private String errorMessage;

    @Column(name="error_details")
    private String errorDetails;

    @Column(name = "error_system")
    private String errorSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_type")
    private ErrorType errorType;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount;

    @Column(name="payload")
    private String payload;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, mappedBy = "esbMessage") //,fetch=FetchType.LAZY
    private List<EsbMessageHeaderEntity> errorHeaders;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getErrorSystem() {
        return errorSystem;
    }

    public void setErrorSystem(String errorSystem) {
        this.errorSystem = errorSystem;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public List<EsbMessageHeaderEntity> getErrorHeaders() {
        return errorHeaders;
    }

    public void setErrorHeaders(List<EsbMessageHeaderEntity> errorHeaders) {
        this.errorHeaders = errorHeaders;
    }

    public EsbMessageHeaderEntity getHeader(String headerName) {
        for (EsbMessageHeaderEntity header : getErrorHeaders()) {
            if (header.getName().equals(headerName)) {
                return header;
            }
        }
        return null;
    }
}

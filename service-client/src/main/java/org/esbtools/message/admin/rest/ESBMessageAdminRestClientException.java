package org.esbtools.message.admin.rest;

public class ESBMessageAdminRestClientException extends RuntimeException {
    public ESBMessageAdminRestClientException() {
    }

    public ESBMessageAdminRestClientException(String message) {
        super(message);
    }

    public ESBMessageAdminRestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ESBMessageAdminRestClientException(Throwable cause) {
        super(cause);
    }
}

package org.esbtools.message.admin.service.extractor;


public class KeyExtractorException extends Exception {

    public KeyExtractorException(String message) {
        super(message);
    }

    public KeyExtractorException (Throwable cause) {
        super (cause);
    }

    public KeyExtractorException(String message, Throwable cause) {
        super(message, cause);
    }
}


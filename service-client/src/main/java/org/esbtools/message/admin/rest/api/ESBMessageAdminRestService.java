package org.esbtools.message.admin.rest.api;

import java.io.IOException;

import org.esbtools.message.admin.model.EsbMessage;

public interface ESBMessageAdminRestService {
    /**
     * Persists an ESB Error Message into the DB
     *
     * @param esbMessage The ESB error message data
     */
    void persistError(EsbMessage esbMessage) throws IOException;
}

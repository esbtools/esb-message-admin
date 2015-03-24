package org.esbtools.message.admin.rest;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.esbtools.message.admin.Provider;
import org.esbtools.message.admin.model.EsbMessage;


/**
 *
 * @author ykoer
 *
 */
@Path("/persist")
@Stateless
public class PersistErrorResourceBean {

    @Inject
    private Instance<Provider> client;

    /**
     * Persists an ESB Error Message into the DB
     *
     * @param esbMessage The ESB error message data
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void persistError(EsbMessage esbMessage) throws IOException {
        client.get().persist(esbMessage);
    }

}

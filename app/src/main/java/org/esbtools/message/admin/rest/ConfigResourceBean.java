package org.esbtools.message.admin.rest;

import org.esbtools.message.admin.EsbMessageAdminService;
import org.esbtools.message.admin.model.MessageSearchConfigurations;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ejb.Stateless;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/config")
@Stateless
public class ConfigResourceBean {

    @Inject
    private Instance<EsbMessageAdminService> client;

    /**
     * Fetches configurations for searching messages
     *
     * @return MessageSearchConfigurations an object containing all
     * configurations for searching messages
     */
    @GET
    @Path("/messageSearch")
    @Produces({MediaType.APPLICATION_JSON})
    public MessageSearchConfigurations getMessageSearchConfigurations() {
        return client.get().getSearchConfigurations();
    }

}

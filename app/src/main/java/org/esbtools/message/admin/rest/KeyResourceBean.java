/*
 Copyright 2014 Red Hat, Inc. and/or its affiliates.

 This file is part of ESB Message Admin.

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
package org.esbtools.message.admin.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.esbtools.message.admin.Provider;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 *
 * @author vrjain
 *
 */
@Path("/key")
@Stateless
public class KeyResourceBean {

    @Inject
    private Instance<Provider> client;
    private final static String formatString = "yyyy-MM-dd'T'HH:mm:ss";
    private final static DateFormat df = new SimpleDateFormat(formatString);
    private final static Logger log = Logger.getLogger(KeyResourceBean.class.getName());

    /**
     * Returns entire metadata keys tree based on the type of the tree requested
     *
     * @param type              type can by sync(Entities) or search(KeyGroups)
     * @return MetadataResult   the entire keys tree
     */
    @GET
    @Path("/tree/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    public MetadataResponse getTree(@PathParam("type") String type) {
        return client.get().getMetadataTree(getType(type));
    }

    /**
     * Add a MetadataField child
     *
     * @param parentId          the id of the MetadataField to add a child to
     * @param name              the name of the child
     * @param type              the type of the child
     * @param value             the value of the child
     * @return MetadataResult   the entire keys tree and the parent field
     */
    @PUT
    @Path("/addChild/{parent_id}")
    @Produces({MediaType.APPLICATION_JSON})
    public MetadataResponse addChild(@PathParam("parent_id") Long parentId,
                                   @QueryParam("name") String name,
                                   @QueryParam("type") String type,
                                   @QueryParam("value") String value) {
        return client.get().addChildMetadataField(parentId, name, getType(type), value);
    }

    /**
     * update a MetadataField child
     * @param id                the id of the MetadataField to update
     * @param name              the name of the child
     * @param type              the type of the child
     * @param value             the value of the child
     * @return MetadataResult   the entire keys tree and the parent field
     */
    @PUT
    @Path("/update/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public MetadataResponse update(@PathParam("id") Long id,
                                 @QueryParam("name") String name,
                                 @QueryParam("type") String type,
                                 @QueryParam("value") String value) {
        return client.get().updateMetadataField(id, name, getType(type), value);
    }

    /**
     * delete a MetadataField child
     *
     * @param id                the id of the MetadataField to delete
     * @return MetadataResult   the entire keys tree and the parent field
     */
    @PUT
    @Path("/delete/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public MetadataResponse delete(@PathParam("id") Long id) {
        return client.get().deleteMetadataField(id);
    }

    /**
     * sync an entity
     *
     * @param entity           the entity to sync
     * @param system           the system to sync from
     * @param key              the key using which to sync the entity
     * @param value            the values to sync
     */
    @POST
    @Path("/sync/{entity}/{system}/{key}")
    @Produces({MediaType.APPLICATION_JSON})
    public void sync(@PathParam("entity") String entity,
                               @PathParam("system") String system,
                               @PathParam("key") String key,
                               @QueryParam("values") String values) {

        if(entity!=null && entity.length()>0 &&
           system!=null && system.length()>0 &&
           key!=null && key.length()>0 &&
           values!=null && values.length()>0) {
           String valueArray[] = values.split(",");
           client.get().sync(entity, system, key, valueArray);
        }
    }

    @GET
    @Path("/suggest/")
    @Produces({MediaType.APPLICATION_JSON})
    @JsonInclude(Include.NON_EMPTY)
    public Map<String, List<String>> getAllKeysAndValues() throws IOException {
        return client.get().getSearchKeyValueSuggestions();
    }

    private MetadataType getType(String name) {
        MetadataType typeEnumValue = null;
        try {
            typeEnumValue = MetadataType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unrecognized Metadata Type:" + name);
        }
        return typeEnumValue;
    }
}

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

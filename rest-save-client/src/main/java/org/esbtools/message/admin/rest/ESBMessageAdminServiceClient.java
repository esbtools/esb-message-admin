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
package org.esbtools.message.admin.rest;

import java.io.IOException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.rest.api.ESBMessageAdminRestService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ESBMessageAdminServiceClient implements ESBMessageAdminRestService {
    private final CloseableHttpClient httpClient;
    private final ObjectMapper mapper;

    /** Should not end with a '/' character */
    private final String esbMessageAdminContext;

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    /**
     * Uses the statically-shared, default {@link com.fasterxml.jackson.databind.ObjectMapper},
     * {@link #DEFAULT_OBJECT_MAPPER}.
     *
     * @param esbMessageAdminContext The context of the application or service. For example if an API call to this service
     *         is {@code "http://esbmessageadmin.esbtools.org/api/persist"} then you should pass {@code "http://esbmessageadmin.esbtools.org"}
     *         as the esbMessageAdminContext parameter.
     */
    public ESBMessageAdminServiceClient(CloseableHttpClient httpClient, String esbMessageAdminContext) {
        this(httpClient, esbMessageAdminContext, DEFAULT_OBJECT_MAPPER);
    }

    /**
     * Allows injecting a different {@link com.fasterxml.jackson.databind.ObjectMapper} to use.
     *
     * @param esbMessageAdminContext The context of the application or service. For example if an API call to this service
     *         is {@code "http://esbmessageadmin.esbtools.org/api/persist"} then you should pass {@code "http://esbmessageadmin.esbtools.org"}
     *         as the esbMessageAdminContext parameter.
     */
    public ESBMessageAdminServiceClient(CloseableHttpClient httpClient, String esbMessageAdminContext, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.esbMessageAdminContext = esbMessageAdminContext.replaceAll("/+$", "");
        this.mapper = mapper;
    }

    /**
     * An {@literal @}{@link javax.inject.Inject}able constructor that allows injecting the service client via CDI. The
     * {@code esbMessageAdminContext} must be available as a String with the {@literal @}{@link javax.inject.Named} qualifier of
     * "esbMessageAdminContext".
     *
     * @param esbMessageAdminContext The context of the application or service. For example if an API call to this service
     *         is {@code "http://esbmessageadmin.esbtools.org/api/persist"} then you should pass {@code "http://esbmessageadmin.esbtools.org"}
     *         as the esbMessageAdminContext parameter.
     * @param mapper An parameterized {@link javax.enterprise.inject.Instance} so that a bean may be optionally bound
     *         to {@link com.fasterxml.jackson.databind.ObjectMapper}. If none is explicitly bound the default will be
     *         used: {@link #DEFAULT_OBJECT_MAPPER}.
     */
    @Inject
    public ESBMessageAdminServiceClient(CloseableHttpClient httpClient, @Named("esbMessageAdminContext") String esbMessageAdminContext,
                               Instance<ObjectMapper> mapper) {
        this.httpClient = httpClient;
        this.esbMessageAdminContext = esbMessageAdminContext.replaceAll("/+$", "");

        if (mapper.isUnsatisfied()) {
            this.mapper = DEFAULT_OBJECT_MAPPER;
        } else {
            this.mapper = mapper.get();
        }
    }

    @Override
    public void persistError(EsbMessage esbMessage) throws IOException {
        doRequest(RequestBuilder.post()
                .setUri(serviceUrl("/persist"))
                .setEntity(asEntity(esbMessage))
                .build());
    }

    protected void doRequest(HttpUriRequest request) throws IOException {
        httpClient.execute(request).close();
    }

    protected String serviceUrl(String path) {
        return esbMessageAdminContext + "/api" + path;
    }

    protected HttpEntity asEntity(Object object) {
        try {
            String serialized = mapper.writeValueAsString(object);
            return new StringEntity(serialized, ContentType.APPLICATION_JSON);
        } catch (JsonProcessingException e) {
            throw new ESBMessageAdminRestClientException(e);
        }
    }
}

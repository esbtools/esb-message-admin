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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchField;
import org.esbtools.message.admin.model.SearchResult;
import org.esbtools.message.admin.spi.Provider;


/**
 *
 * @author ykoer
 *
 */
@Path("/search")
@Stateless
public class SearchErrorResourceBean {

    @Inject
    private Instance<Provider> client;
    private final static String formatString = "yyyy-MM-dd'T'HH:mm:ss";
    private final static DateFormat df = new SimpleDateFormat(formatString);
    private final static Logger log = Logger.getLogger(SearchErrorResourceBean.class.getName());

    /**
     * Returns error messages from a given queue
     * To be deleted once the criteria API is ready to be used.
     *
     * @param queue    the error queue
     * @param fromDate the start timestamp of the range
     * @param toDate   the end timestamp of the range
     * @param start    sets the position of the first result to retrieve
     * @param results  sets the maximum number of results to retrieve
     * @return ESB Error info
     */
    @GET
    @Path("/queue/{queue}")
    @Deprecated
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult getErrorsByQueueName(@PathParam("queue") String queue,
                                             @QueryParam("fromDate") String fromDate,
                                             @QueryParam("toDate") String toDate,
                                             @QueryParam("start") Integer start,
                                             @QueryParam("results") Integer maxResults) throws IOException {
        SearchCriteria criteria = new SearchCriteria(SearchField.errorQueue, queue);
        return client.get().searchMessagesByCriteria(criteria, getDate(fromDate), getDate(toDate), start, maxResults);
    }

    @GET
    @Path("/criteria/{search_criteria}")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult getErrorsByCriteria(@PathParam("search_criteria") PathSegment argCriteria,
                                            @QueryParam("fromDate") String fromDate,
                                            @QueryParam("toDate") String toDate,
                                            @QueryParam("start") Integer start,
                                            @QueryParam("results") Integer maxResults) {
        SearchCriteria criteria = getCriteria(argCriteria);
        log.info("result:" + criteria);
        return client.get().searchMessagesByCriteria(criteria, getDate(fromDate), getDate(toDate), start, maxResults);
    }

    @GET
    @Path("/id/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult getErrorById(@PathParam("id") Long id) throws IOException {
        return client.get().getMessageById(id);
    }

    private Date getDate(String stringDate) {
        try {
            return df.parse(stringDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format:" + stringDate + " expected format:" + formatString);
        }
    }

    private SearchCriteria getCriteria(PathSegment search_criteria) {
        SearchCriteria criteria = new SearchCriteria();
        List<Criterion> criteriaList = new ArrayList<Criterion>();
        MultivaluedMap<String, String> map = search_criteria.getMatrixParameters();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                Criterion criterion = new Criterion();
                if (!SearchField.isPreDefined(entry.getKey())) {
                    criterion.setCustomKey(entry.getKey());
                    criterion.setValue(value);
                } else {
                    criterion.setField(SearchField.valueOf(entry.getKey()));
                    if (criterion.getField().getValueType() == String.class) {
                        criterion.setValue(value);
                    } else {
                        // only other value is long
                        criterion.setValue(Long.parseLong(value));
                    }
                }
                criteriaList.add(criterion);
            }
        }
        criteria.setCriteria(criteriaList.toArray(new Criterion[0]));
        log.log(Level.FINE, criteria.toString());
        return criteria;
    }
}

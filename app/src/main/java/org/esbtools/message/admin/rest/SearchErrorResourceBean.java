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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang3.StringUtils;
import org.esbtools.message.admin.EsbMessageAdminService;
import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchField;
import org.esbtools.message.admin.model.SearchResult;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/**
 *
 * @author ykoer
 *
 */
@Path("/search")
@Stateless
public class SearchErrorResourceBean {

    @Inject
    private Instance<EsbMessageAdminService> client;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final Logger LOGGER= LoggerFactory.getLogger(SearchErrorResourceBean.class);


    @GET
    @Path("/criteria/{search_criteria}")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult getErrorsByCriteria(@PathParam("search_criteria") PathSegment argCriteria,
                                            @QueryParam("fromDate") String fromDate,
                                            @QueryParam("toDate") String toDate,
                                            @QueryParam("sortField") String sortField,
                                            @QueryParam("sortAsc") Boolean sortAsc,
                                            @QueryParam("start") Integer start,
                                            @QueryParam("results") Integer maxResults) {
        SearchCriteria criteria = getCriteria(argCriteria);

        String sortBy = StringUtils.isBlank(sortField) ? "timestamp" : sortField;
        Boolean sortAscending = sortAsc==null ? true : sortAsc;

        LOGGER.info("search criteria: {}  sortBy: {} asc=: {}", criteria, sortBy, sortAscending);
        return client.get().searchMessagesByCriteria(criteria, getDate(fromDate), getDate(toDate), sortBy, sortAscending, start, maxResults);
    }

    @GET
    @Path("/id/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public SearchResult getErrorById(@PathParam("id") Long id) throws IOException {
        return client.get().getMessageById(id);
    }

    private Date getDate(String stringDate) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(stringDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format:" + stringDate + " expected format:" + DATE_FORMAT, e);
        }
    }

    private SearchCriteria getCriteria(PathSegment searchCriteria) {
        SearchCriteria criteria = new SearchCriteria();
        List<Criterion> criteriaList = new ArrayList<Criterion>();
        MultivaluedMap<String, String> map = searchCriteria.getMatrixParameters();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            for (String value : entry.getValue()) {
                Criterion criterion = new Criterion();
                if (!SearchField.isPreDefined(entry.getKey())) {
                    criterion.setCustomKey(entry.getKey());
                    criterion.setValue(value);
                } else {
                    getPredefinedCriteria(entry, value, criterion);
                }
                criteriaList.add(criterion);
            }
        }
        criteria.setCriteria(criteriaList.toArray(new Criterion[0]));
        LOGGER.debug("criteria: {}", criteria.toString());
        return criteria;
    }

    private void getPredefinedCriteria(Map.Entry<String, List<String>> entry, String value, Criterion criterion) {
        criterion.setField(SearchField.match(entry.getKey()));
        if (criterion.getField().getValueType() == String.class) {
            criterion.setValue(value);
        } else {
            // only other value is long
            criterion.setValue(Long.parseLong(value));
        }
    }
}

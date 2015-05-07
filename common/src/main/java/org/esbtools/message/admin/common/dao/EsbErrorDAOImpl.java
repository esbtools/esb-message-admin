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
package org.esbtools.message.admin.common.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.esbtools.message.admin.common.Configuration;
import org.esbtools.message.admin.common.ConversionUtility;
import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.common.orm.EsbMessageHeaderEntity;
import org.esbtools.message.admin.common.orm.EsbMessageSensitiveInfoEntity;
import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.HeaderType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class EsbErrorDAOImpl implements EsbErrorDAO {

    private final EntityManager mgr;
    private final static Logger log = Logger.getLogger(EsbErrorDAOImpl.class.getName());

    private static final String MESSAGE_PROPERTY_PAYLOAD_HASH = "esbPayloadHash";

    private Set<String> sortingFields = new HashSet<>();
    private List<Configuration> nonViewableConfiguration = null;
    private List<Configuration> partiallyViewableConfiguration = null;
    public EsbErrorDAOImpl(EntityManager mgr, JSONObject config) {
        this.mgr=mgr;
        JSONArray sortFields = (JSONArray) config.get("sortingFields");
        if(sortFields!=null) {
            for(int i=0;i<sortFields.size();i++) {
                sortingFields.add(sortFields.get(i).toString());
            }
        }
        nonViewableConfiguration = ConversionUtility.getConfigurations((JSONArray) config.get("nonViewableMessages"));
        partiallyViewableConfiguration = ConversionUtility.getConfigurations((JSONArray) config.get("partiallyViewableMessages"));
    }

    /**
     * {@inheritDoc}
     */
    public void create(EsbMessage em, Map<String, List<String>> extractedHeaders) {

        EsbMessageEntity eme = ConversionUtility.convertFromEsbMessage(em);

        Map<String,String> matchedConfiguration = matchCriteria(em, partiallyViewableConfiguration);
        if(matchedConfiguration!=null) {
            String parentTag = matchedConfiguration.get("sensitiveTag");
            Pattern pattern = Pattern.compile("<("+parentTag+")>((?!<("+parentTag+")>).)*</("+parentTag+")>");
            Matcher matcher = pattern.matcher(em.getPayload());
            ArrayList<EsbMessageSensitiveInfoEntity> sensitiveInformation = new ArrayList<>();
            while(matcher.find()) {
                sensitiveInformation.add(new EsbMessageSensitiveInfoEntity(eme, matcher.group(0)) );
            }
            matcher.reset();
            String test = matcher.replaceAll("<$1>"+matchedConfiguration.get("replacementText")+"</$1>");
            eme.setPayload(test);
        }

        for (Entry<String, List<String>> headerSet : extractedHeaders.entrySet()) {
            for(String value : headerSet.getValue()) {
                EsbMessageHeaderEntity extractedHeader= new EsbMessageHeaderEntity();
                extractedHeader.setName(headerSet.getKey());
                extractedHeader.setType(HeaderType.METADATA);
                extractedHeader.setValue(value);
                extractedHeader.setEsbMessage(eme);
                eme.getErrorHeaders().add(extractedHeader);
            }
        }

        // check if message(s) with the same payload hash exists already
        EsbMessageHeaderEntity payloadHash = eme.getHeader(MESSAGE_PROPERTY_PAYLOAD_HASH);

        // loop through all the messages with the same payload hash, sum all the occurrence counts together and remove them together with the headers.
        // we will create a new entity with the increased occurrence count
        if (payloadHash != null) {
            List<EsbMessageEntity> messages = getMessagesByPayloadHash(payloadHash.getValue());

            int occurrenceCount = 0;
            for (int i=0; i<messages.size(); i++) {
                mgr.remove(messages.get(i));
                occurrenceCount += messages.get(i).getOccurrenceCount();
            }
            eme.setOccurrenceCount(++occurrenceCount);
        }
        mgr.persist(eme);
    }

    @Override
    public SearchResult findMessagesBySearchCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, Boolean sortAsc, Integer start, Integer maxResults) {

        SearchResult result = new SearchResult();

        // allow sorting only by display fields, choose time stamp if proper field is not set.
        if(sortField==null || !sortingFields.contains(sortField)) {
            sortField = "timestamp";
        }
        if (maxResults > 0) {
            Query countQuery = getQueryFromCriteria(criteria, sortField, sortAsc, fromDate, toDate, true);
            try {
                result.setTotalResults((Long) countQuery.getSingleResult());
            } catch (NoResultException e) {
                return SearchResult.empty();
            }

            Query resultQuery = getQueryFromCriteria(criteria, sortField, sortAsc, fromDate, toDate, false);

            resultQuery.setFirstResult(start);
            resultQuery.setMaxResults(maxResults);
            @SuppressWarnings("rawtypes")
            List searchResult = resultQuery.getResultList();

            EsbMessage[] resultMessages = new EsbMessage[searchResult.size()];
            for (int i = 0; i < resultMessages.length; i++) {
                Object cols[] = (Object[]) searchResult.get(i);
                EsbMessage msg = new EsbMessage();
                msg.setId((Long) cols[0]);
                msg.setTimestamp((Date) cols[1]);
                msg.setMessageType((String) cols[2]);
                msg.setSourceSystem((String) cols[3]);
                msg.setErrorSystem((String) cols[4]);
                msg.setOccurrenceCount((Integer) cols[5]);
                resultMessages[i] = msg;
            }
            result.setMessages(resultMessages);
            result.setItemsPerPage(maxResults);
            result.setPage((start / maxResults) + 1);
        } else {
            result.setItemsPerPage(0);
            result.setPage(0);
        }


        return result;
    }

    private Query getQueryFromCriteria(SearchCriteria criteria, String sortField, boolean sortAsc, Date fromDate, Date toDate, boolean countQuery) {
        // to do : read display fields from a config file and set select fields only on result object.
        String projection = (countQuery) ? " count( distinct e.id) " : " distinct e.id, e.timestamp, e.messageType, e.sourceSystem, e.errorSystem, e.occurrenceCount ";
        StringBuilder queryBuilder = new StringBuilder("select" + projection + "from EsbMessageEntity e ");

        int i = 0;
        StringBuilder predefWhereClause = new StringBuilder(""), customWhereClause = new StringBuilder(""), customJoins = new StringBuilder("");
        for (Criterion crit : criteria.getCriteria()) {
            if (!crit.isCustom()) {
                predefWhereClause.append("and UPPER(e." + crit.getKeyString() + ") = :" + crit.getField().name() + " ");
            } else {
                customJoins.append("join e.errorHeaders h" + i + " ");
                customWhereClause.append("and UPPER(h" + i + ".name) = '" + crit.getKeyString().toUpperCase() + "' and UPPER(h" + i + ".value) = '" + crit.getStringValue().toUpperCase() + "' ");
                i++;
            }
        }
        queryBuilder.append(customJoins.toString());
        queryBuilder.append("where e.timestamp between :fromDate AND :toDate ");
        queryBuilder.append(predefWhereClause.toString());
        queryBuilder.append(customWhereClause.toString());
        if(!countQuery) {
            queryBuilder.append("order by e."+sortField);
            if(sortAsc) {
                queryBuilder.append(" asc");
            } else {
                queryBuilder.append(" desc");
            }
        }
        log.info(queryBuilder.toString());
        Query query = mgr.createQuery(queryBuilder.toString());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        for (Criterion crit : criteria.getCriteria()) {
            if (!crit.isCustom()) {
                if (crit.getField().getValueType() == String.class) {
                    query.setParameter(crit.getField().name(), crit.getStringValue().toUpperCase());
                } else {
                    query.setParameter(crit.getField().name(), crit.getLongValue());
                }
            }
        }

        return query;
    }

    @Override
    public SearchResult getMessageById(Long id) {
        SearchResult result = new SearchResult();

        Query query = mgr.createQuery("select e from EsbMessageEntity e where e.id = :id");
        query.setParameter("id", id);
        List<EsbMessageEntity> messages = (List<EsbMessageEntity>) query.getResultList();
        if (messages.size() == 0) {
            result.setTotalResults(0);
        } else {
            result.setTotalResults(1);
            EsbMessage[] messageArray = new EsbMessage[1];
            messageArray[0] = ConversionUtility.convertToEsbMessage(messages.get(0));
            Map<String,String> matchedConfiguration = matchCriteria(messageArray[0], nonViewableConfiguration);
            if(matchedConfiguration!=null) {
                messageArray[0].setPayload(matchedConfiguration.get("replaceMessage"));
            }
            result.setMessages(messageArray);
        }
        return result;
    }

    private Map<String,String> matchCriteria(EsbMessage message, List<Configuration> configurations) {
        String messageString = message.toString().toLowerCase();
        for(Configuration conf: configurations) {
            boolean matched = true;
            for(Entry<String,String> matchCondition: conf.getMatchCriteriaMap().entrySet()) {
                if(!messageString.contains(matchCondition.toString().toLowerCase())) {
                    matched = false;
                    break;
                }
            }
            if(matched) {
                return conf.getConfigurationMap();
            }
        }
        return null;
    }

    public List<EsbMessageEntity> getMessagesByPayloadHash(String payloadHash) {

        Query query = mgr.createQuery("select e from EsbMessageEntity e join e.errorHeaders h where h.name = :name and h.value = :hash order by e.timestamp");
        query.setParameter("name", MESSAGE_PROPERTY_PAYLOAD_HASH);
        query.setParameter("hash", payloadHash);
        return (List<EsbMessageEntity>) query.getResultList();
    }

}

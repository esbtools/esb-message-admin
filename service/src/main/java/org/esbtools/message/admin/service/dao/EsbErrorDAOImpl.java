/**
 * Copyright (c) 2006 Red Hat, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Red Hat, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Red Hat.
 */
package org.esbtools.message.admin.service.dao;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.HeaderType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.esbtools.message.admin.service.extractor.KeyExtractorException;
import org.esbtools.message.admin.service.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.service.orm.EsbMessageEntity;
import org.esbtools.message.admin.service.orm.EsbMessageHeaderEntity;


public class EsbErrorDAOImpl implements EsbErrorDAO {

    private final EntityManager mgr;
    private final static Logger log = Logger.getLogger(EsbErrorDAOImpl.class.getName());

    private static final String MESSAGE_PROPERTY_PAYLOAD_HASH = "esbPayloadHash";

    private Properties config;

    public EsbErrorDAOImpl(EntityManager mgr) {
        this.mgr=mgr;

        try {
            config = new Properties();
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            config.load(in);
            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void create(EsbMessageEntity eme, KeyExtractorUtil extractor) {

        try {
            Map<String, List<String>> extractedHeaders = extractor.getEntriesFromPayload(eme.getPayload());
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
        } catch (KeyExtractorException e) {
            log.warning("Could not extract metadata! " + e);
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
    public SearchResult findMessagesBySearchCriteria(SearchCriteria criteria, Date fromDate, Date toDate, Integer start, Integer maxResults) {

        SearchResult result = new SearchResult();

        if (maxResults > 0) {
            Query countQuery = getQueryFromCriteria(criteria, fromDate, toDate, true);
            try {
                result.setTotalResults((Long) countQuery.getSingleResult());
            } catch (NoResultException e) {
                return SearchResult.empty();
            }

            Query resultQuery = getQueryFromCriteria(criteria, fromDate, toDate, false);

            resultQuery.setFirstResult(start);
            resultQuery.setMaxResults(maxResults);

            EsbMessage[] resultMessages = new EsbMessage[resultQuery.getResultList().size()];

            for (int i = 0; i < resultQuery.getResultList().size(); i++) {
                Object[] cols = (Object[]) resultQuery.getResultList().get(i);
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

    private Query getQueryFromCriteria(SearchCriteria criteria, Date fromDate, Date toDate, boolean countQuery) {
        String projection = (countQuery) ? " count( distinct e.id) " : " distinct e.id, e.timestamp, e.messageType, e.sourceSystem, e.errorSystem, e.occurrenceCount ";
        int totalCustomKeys = numberOfCustomKeys(criteria);
        StringBuilder queryBuilder = new StringBuilder("select" + projection + "from EsbMessageEntity e ");
        for (int i = 0; i < totalCustomKeys; i++) {
            queryBuilder.append("join e.errorHeaders h" + i + " ");
        }
        queryBuilder.append("where e.timestamp between :fromDate AND :toDate ");
        for (Criterion crit : criteria.getCriteria()) {
            if (!crit.isCustom())
                queryBuilder.append("and e." + crit.getKeyString() + " = :" + crit.getField().name() + " ");
        }
        int i = 0;
        for (Criterion crit : criteria.getCriteria()) {
            if (crit.isCustom()) {
                queryBuilder.append("and h" + i + ".name ='" + crit.getKeyString() + "' and h" + i + ".value = '" + crit.getStringValue() + "' ");
                i++;
            }
        }
        log.info(queryBuilder.toString());
        Query query = mgr.createQuery(queryBuilder.toString());
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        for (Criterion crit : criteria.getCriteria()) {
            if (!crit.isCustom()) {
                if (crit.getField().getValueType() == String.class) {
                    query.setParameter(crit.getField().name(), crit.getStringValue());
                } else {
                    query.setParameter(crit.getField().name(), crit.getLongValue());
                }
            }
        }

        return query;
    }

    private int numberOfCustomKeys(SearchCriteria criteria) {
        int result = 0;
        for (Criterion critieron : criteria.getCriteria()) {
            if (critieron.isCustom())
                result++;
        }
        return result;
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
            result.setMessages(messageArray);
        }
        return result;
    }

    public List<EsbMessageEntity> getMessagesByPayloadHash(String payloadHash) {

        Query query = mgr.createQuery("select e from EsbMessageEntity e join e.errorHeaders h where h.name = :name and h.value = :hash order by e.timestamp");
        query.setParameter("name", MESSAGE_PROPERTY_PAYLOAD_HASH);
        query.setParameter("hash", payloadHash);
        return (List<EsbMessageEntity>) query.getResultList();
    }

}

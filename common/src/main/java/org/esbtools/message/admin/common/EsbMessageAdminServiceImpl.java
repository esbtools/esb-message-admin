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
package org.esbtools.message.admin.common;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.esbtools.message.admin.Provider;
import org.esbtools.message.admin.common.config.VisibilityConfiguration;
import org.esbtools.message.admin.common.extractor.KeyExtractorException;
import org.esbtools.message.admin.common.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.common.orm.AuditEventEntity;
import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.common.orm.EsbMessageHeaderEntity;
import org.esbtools.message.admin.common.orm.MetadataEntity;
import org.esbtools.message.admin.common.utility.ConversionUtility;
import org.esbtools.message.admin.common.utility.EncryptionUtility;
import org.esbtools.message.admin.model.AuditEvent;
import org.esbtools.message.admin.model.Criterion;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.Header;
import org.esbtools.message.admin.model.HeaderType;
import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.esbtools.message.admin.common.config.EMAConfiguration.getEncryptionKey;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getNonViewableMessages;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getPartiallyViewableMessages;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getResyncRestEndpoints;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getSortingFields;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getSuggestedFields;
import static org.esbtools.message.admin.common.config.EMAConfiguration.getEditableMessageTypes;

@Named
public class EsbMessageAdminServiceImpl implements Provider {

    private static final Logger LOG = LoggerFactory.getLogger(EsbMessageAdminServiceImpl.class);
    private static final String ERROR_KEY_TYPE = "error";
    private static final String MESSAGE_PROPERTY_PAYLOAD_HASH = "esbPayloadHash";
    private static final String ILLEGAL_ARGUMENT = "Illegal Argument:";
    private static final String DEFAULT_USER = "someUser";
    private static final String METADATA_KEY_TYPE = "metadata";
    private static final String TYPE_PLACEHOLDER = "$TYPE";
    private static final String METADATA_QUERY = "select f from MetadataEntity f where f.type = '" + TYPE_PLACEHOLDER + "'";
    private static transient KeyExtractorUtil extractor;
    private static transient EncryptionUtility encryptor;
    private static transient Map<MetadataType, MetadataResponse> treeCache = new ConcurrentHashMap<>();
    private static transient Map<String, List<String>> suggestionsCache = new ConcurrentHashMap<>();

    @Inject
    private EntityManager entityMgr;

    void setErrorEntityManager(EntityManager entityMgr) {
        this.entityMgr = entityMgr;
    }

    private KeyExtractorUtil getKeyExtractor() {

        MetadataResponse searchKeyResponse = getMetadataTree(MetadataType.SearchKeys);
        if (extractor == null || !extractor.getHash().contentEquals(searchKeyResponse.getHash())) {
            List<MetadataField> searchKeys = (searchKeyResponse.getTree() != null) ? searchKeyResponse.getTree().getChildren() : new ArrayList<MetadataField>();
            extractor = new KeyExtractorUtil(searchKeys, searchKeyResponse.getHash());
        }
        return extractor;
    }

    private EncryptionUtility getEncryptor() {
        if (encryptor == null) {
            encryptor = new EncryptionUtility(getEncryptionKey());
        }
        return encryptor;
    }

    @Override
    public void persist(EsbMessage esbMessage) throws IOException {

        Map<String, List<String>> extractedHeaders = null;

        try {
            extractedHeaders = getKeyExtractor().getEntriesFromPayload(esbMessage.getPayload());
        } catch (KeyExtractorException e) {
            LOG.warn("Could not extract metadata for ebMessage {} ", esbMessage, e);
            extractedHeaders = new HashMap<>();
        }

        create(esbMessage, extractedHeaders);
        ensureSuggestionsArePresent(esbMessage, extractedHeaders);

    }

    @Override
    public void persist(EsbMessage[] esbMessages) throws IOException {
        for (EsbMessage esbMessage : esbMessages) {
            persist(esbMessage);
        }
    }

    @Override
    public void update(EsbMessage esbMessage) {
        updateMessage(esbMessage);
    }

    @Override
    public void update(EsbMessage[] esbMessages) {
        for (EsbMessage esbMessage : esbMessages) {
            update(esbMessage);
        }
    }

    @Override
    public SearchResult searchMessagesByCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, boolean sortAsc, int start, int maxResults) {

        Date from;

        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, -30);
            from = c.getTime();
        } else {
            from = fromDate;
        }
        Date to = toDate == null ? new Date() : toDate;

        return findMessagesBySearchCriteria(criteria, from, to, sortField, sortAsc, start, maxResults);
    }

    /**
     * Saves audit event
     * @param auditEvent
     */
    public void saveAuditEvent(AuditEvent auditEvent) {
        AuditEventEntity event = new AuditEventEntity(auditEvent);
        entityMgr.persist(event);
    }

    /**
     * Creates a new EsbError entity
     * @param esbMessage
     * @param extractedHeaders
     */
    public void create(EsbMessage esbMessage, Map<String, List<String>> extractedHeaders) {

        EsbMessageEntity eme = ConversionUtility.convertFromEsbMessage(esbMessage);
        maskSensitiveInfo(esbMessage, eme);

        extractHeaders(extractedHeaders, eme);

        // check if message(s) with the same payload hash exists already
        EsbMessageHeaderEntity payloadHash = eme.getHeader(MESSAGE_PROPERTY_PAYLOAD_HASH);

        // loop through all the messages with the same payload hash, sum all the occurrence counts together and remove them together with the headers.
        // we will create a new entity with the increased occurrence count
        if (payloadHash != null) {
            List<EsbMessageEntity> messages = getMessagesByPayloadHash(payloadHash.getValue());

            int occurrenceCount = 0;
            for (int i=0; i<messages.size(); i++) {
                entityMgr.remove(messages.get(i));
                occurrenceCount += messages.get(i).getOccurrenceCount();
            }
            eme.setOccurrenceCount(++occurrenceCount);
        }
        entityMgr.persist(eme);
    }

    public void updateMessage(EsbMessage esbMessage) {

        // explicitly check if the loaded message is in our list of allowed message types
        if( isEditableMessage(esbMessage) ){
            EsbMessageEntity persistedMessage = entityMgr.find(EsbMessageEntity.class, esbMessage.getId());
            // scold the user for trying to update instead of insert
            if( persistedMessage == null ){
                throw new IllegalArgumentException("Message {\n}"+esbMessage.toString()+"\n} does not exist in backend store, cannot update.");
            }
            // Update the payload. we're going to resend the payload to the gateway anyway, so no sense in messing with headers
            persistedMessage.setPayload(esbMessage.getPayload());
            entityMgr.flush();
        }else{
            LOG.warn( "User was attempting to edit a message of type " + esbMessage.getMessageType() + "." );
            throw new IllegalArgumentException("Message is not eligible for editation."); // generic exception
        }
    }

    private void maskSensitiveInfo(EsbMessage em, EsbMessageEntity eme) {
        em.setPayload(em.getPayload().replaceAll("\n", ""));
        em.setPayload(em.getPayload().replaceAll("\r", ""));
        em.setPayload(em.getPayload().replaceAll("\t", ""));
        em.setPayload(em.getPayload().replaceAll(">\\s*<", "><"));
        Map<String,String> matchedConfiguration = matchCriteria(em, getPartiallyViewableMessages());
        if(matchedConfiguration!=null) {
            String parentTag = matchedConfiguration.get("sensitiveTag");
            Pattern pattern = Pattern.compile("<("+parentTag+")>((?!<("+parentTag+")>).)*</("+parentTag+")>");
            Matcher matcher = pattern.matcher(em.getPayload());
            List<String> sensitiveInformation = new ArrayList<>();
            while(matcher.find()) {
                sensitiveInformation.add(matcher.group(0));
            }
            matcher.reset();
            String maskedText = matcher.replaceAll("<$1>"+matchedConfiguration.get("replacementText")+"</$1>");
            eme.setErrorSensitiveInfo(ConversionUtility.convertToEsbMessageSensitiveInfo(getEncryptor(), eme, sensitiveInformation));
            eme.setPayload(maskedText);
        }
    }

    private void extractHeaders(Map<String, List<String>> extractedHeaders, EsbMessageEntity eme) {
        for (Map.Entry<String, List<String>> headerSet : extractedHeaders.entrySet()) {
            for(String value : headerSet.getValue()) {
                EsbMessageHeaderEntity extractedHeader= new EsbMessageHeaderEntity();
                extractedHeader.setName(headerSet.getKey());
                extractedHeader.setType(HeaderType.METADATA);
                extractedHeader.setValue(value);
                extractedHeader.setEsbMessage(eme);
                eme.getErrorHeaders().add(extractedHeader);
            }
        }
    }

    public List<EsbMessageEntity> getMessagesByPayloadHash(String payloadHash) {

        Query query = entityMgr.createQuery("select e from EsbMessageEntity e join e.errorHeaders h where h.name = :name and h.value = :hash order by e.timestamp");
        query.setParameter("name", MESSAGE_PROPERTY_PAYLOAD_HASH);
        query.setParameter("hash", payloadHash);
        return (List<EsbMessageEntity>) query.getResultList();
    }

    /**
     * Returns error message of the given queue
     * @param criteria
     * @param fromDate
     * @param toDate
     * @param sortField
     * @param sortAsc
     * @param start
     * @param maxResults
     * @return SearchResult
     */
    public SearchResult findMessagesBySearchCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, Boolean sortAsc, Integer start, Integer maxResults) {

        SearchResult result = new SearchResult();
        long startTime = System.currentTimeMillis();

        // allow sorting only by display fields, choose time stamp if proper field is not set.
        String sortBy = (sortField==null || !getSortingFields().contains(sortField)) ? "timestamp" : sortField;

        if (maxResults > 0) {
            Query countQuery = getQueryFromCriteria(criteria, sortBy, sortAsc, fromDate, toDate, true);
            try {
                result.setTotalResults((Long) countQuery.getSingleResult());
            } catch (NoResultException e) {
                LOG.warn("No result when trying to do count of searchResults", e);
                return SearchResult.empty();
            }

            Query resultQuery = getQueryFromCriteria(criteria, sortBy, sortAsc, fromDate, toDate, false);

            resultQuery.setFirstResult(start);
            resultQuery.setMaxResults(maxResults);
            @SuppressWarnings("rawtypes")
            List searchResult = resultQuery.getResultList();

            EsbMessage[] resultMessages = new EsbMessage[searchResult.size()];
            for (int i = 0; i < resultMessages.length; i++) {
                Object[] cols = (Object[]) searchResult.get(i);
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
        long endTime = System.currentTimeMillis();
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "SEARCH", ERROR_KEY_TYPE, "", "", criteria.toString() + ", From:" + fromDate + ", To:" + toDate + ", Sort:" + sortField + ", Asc:" + sortAsc + ", start:" + start + ", maxResults:" + maxResults + " time:" + (endTime - startTime)));

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
            queryBuilder.append("order by e." + sortField);
            if(sortAsc) {
                queryBuilder.append(" asc");
            } else {
                queryBuilder.append(" desc");
            }
        }
        LOG.info("queryBuilder: {}", queryBuilder.toString());
        Query query = entityMgr.createQuery(queryBuilder.toString());
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

    /**
     * Returns the error message given the id
     * @param id
     * @return SearchResult
     */
    @Override
    public SearchResult getMessageById(Long id) {
        SearchResult result = new SearchResult();

        Query query = entityMgr.createQuery("select e from EsbMessageEntity e where e.id = :id");
        query.setParameter("id", id);
        List<EsbMessageEntity> messages = (List<EsbMessageEntity>) query.getResultList();
        if (messages.isEmpty()) {
            result.setTotalResults(0);
        } else {
            result.setTotalResults(1);
            EsbMessage[] messageArray = new EsbMessage[1];
            messageArray[0] = ConversionUtility.convertToEsbMessage(messages.get(0));
            Map<String,String> matchedConfiguration = matchCriteria(messageArray[0], getNonViewableMessages());
            if(matchedConfiguration!=null) {
                messageArray[0].setPayload(matchedConfiguration.get("replaceMessage"));
            }
            result.setMessages(messageArray);
        }
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "FETCH", ERROR_KEY_TYPE, "", "", id.toString()));
        return result;
    }

    private Map<String,String> matchCriteria(EsbMessage message, List<VisibilityConfiguration> configurations) {
        String messageString = message.toString().toLowerCase();
        for(VisibilityConfiguration conf: configurations) {
            boolean matched = true;
            for(Map.Entry<String,String> matchCondition: conf.getMatchCriteriaMap().entrySet()) {
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


    private String getMetadataHash(MetadataType type) {
        String hash = "";
        if(type==MetadataType.SearchKeys || type==MetadataType.Entities) {
            Query query = entityMgr.createQuery(METADATA_QUERY.replace(TYPE_PLACEHOLDER, type.toString()));

            List<MetadataEntity> result = query.getResultList();
            if(result!=null && !result.isEmpty()) {
                hash = (String) result.get(0).getValue();
            }
        }
        return hash;
    }

    private String markTreeDirty(MetadataType metadataType) {
        String hash = null;

        MetadataType type = metadataType.isSearchKeyType() ? MetadataType.SearchKeys : MetadataType.Entities;

        Query query = entityMgr.createQuery(METADATA_QUERY.replace(TYPE_PLACEHOLDER, type.toString()));
        List<MetadataEntity> result = query.getResultList();
        if(result!=null && !result.isEmpty()) {
            hash = UUID.randomUUID().toString();
            result.get(0).setValue(hash);
        }
        return hash;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.esbtools.message.admin.service.dao.MetadataDAO#getMetadataTree(org
     * .esbtools.message.admin.model.MetadataType)
     *
     * fetch all metadata fields based on the the type of the tree requested.
     * Then compute the tree from those fields and respond with the entire tree
     * on the tree field, and the result field as null.
     */
    @Override
    public MetadataResponse getMetadataTree(MetadataType type) {

        MetadataResponse result;
        if(type == MetadataType.Entities || type == MetadataType.SearchKeys) {
            String hash = getMetadataHash(type);
            if(treeCache.containsKey(type) && hash.contentEquals(treeCache.get(type).getHash())) {
                return treeCache.get(type);
            } else {
                result = refreshCache(type, hash);
            }
        } else {
            result = new MetadataResponse();
            result.setErrorMessage(ILLEGAL_ARGUMENT + type + ", Expected: Entities or SearchKeys");
        }
        return result;
    }

    private MetadataResponse refreshCache(MetadataType type, String hash) {
        MetadataResponse result;
        result = new MetadataResponse();
        String inClause = null;
        if (type == MetadataType.Entities) {
            inClause = "('Entities', 'Entity', 'System', 'SyncKey')";
        } else {
            inClause = "('SearchKeys', 'SearchKey', 'XPATH', 'Suggestion')";
        }
        if (inClause != null) {
            Query query = entityMgr.createQuery("select f from MetadataEntity f where f.type in " + inClause);
            List<MetadataEntity> queryResult = (List<MetadataEntity>) query.getResultList();
            result.setTree(makeTree(queryResult));
            result.setHash(hash);
            treeCache.put(type, result);
            if(type == MetadataType.SearchKeys) {
                updateSuggestions(result.getTree());
            }
        }
        return result;
    }

    /*
     * given a list of metadata entities with parent ids, create a tree of
     * Metadafields.
     */
    private static MetadataField makeTree(List<MetadataEntity> entities) {
        MetadataField root = null;
        Map<Long, MetadataField> map = new HashMap<>();
        for (MetadataEntity entity : entities) {
            MetadataField field = ConversionUtility.convertToMetadataField(entity);
            if (entity.getType() == MetadataType.Entities || entity.getType() == MetadataType.SearchKeys) {
                root = field;
                root.setValue(entity.getType().toString());
            }
            map.put(field.getId(), field);
        }
        for (MetadataEntity entity : entities) {
            MetadataField field = map.get(entity.getId());
            MetadataField parent=null;
            if (entity.getParentId().intValue() != -1) {
                parent=map.get(entity.getParentId());
                if(parent != null) {
                    parent.addDescendant(field);
                }
            }
        }
        return root;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.esbtools.message.admin.service.dao.MetadataDAO#addChildMetadataField
     * (java.lang.Long, java.lang.String,
     * org.esbtools.message.admin.service.model.MetadataType, java.lang.String)
     *
     * given a parent id, creates a metadata field and adds the new field as a
     * child of the given parent. returns the entire tree of the metadata type
     * and the parent field with all its children in the result field of the
     * response.
     */
    @Override
    public MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value) {

        MetadataResponse result = new MetadataResponse();
        MetadataEntity curr = new MetadataEntity(type, name, value, parentId);
        if (parentId == -1L) {
            if (type != MetadataType.Entities && type != MetadataType.SearchKeys) {
                result.setErrorMessage(ILLEGAL_ARGUMENT + type + ", If parent = -1, Expected: Entities or SearchKeys");
            } else {
                markTreeDirty(type);
                entityMgr.persist(curr);
                result = getMetadataTree(type);
            }
        } else {
            MetadataField parent = getMetadataField(parentId);
            if(parent==null) {
                result.setErrorMessage(ILLEGAL_ARGUMENT + "parent "+parentId+ " not found!");
            } else if (!curr.canBeChildOf(parent.getType())) {
                result.setErrorMessage(ILLEGAL_ARGUMENT + type + " can not be a child of " + parent.getType());
            } else {
                markTreeDirty(type);
                entityMgr.persist(curr);
                result = createMetadataResult(parent);
            }
        }
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "ADD", METADATA_KEY_TYPE, type.toString(), value, curr.toString()));
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.esbtools.message.admin.service.dao.MetadataDAO#updateMetadataField
     * (java.lang.Long, java.lang.String,
     * org.esbtools.message.admin.service.MetadataType, java.lang.String)
     *
     * given a field id, overwrite the name, type and value of the metadata
     * field return the entire metadata tree and the parent of the field being
     * updated in the result field of the response.
     */
    @Override
    public MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value) {

        MetadataResponse result = new MetadataResponse();
        MetadataEntity entity = entityMgr.find(MetadataEntity.class, id);

        if (entity == null) {
            result.setErrorMessage("Entity not found:" + id);
        } else {
            MetadataField parent = getMetadataField(entity.getParentId());
            if (parent == null) {
                result.setErrorMessage("Parent (" + entity.getParentId() + ") of Entity " + id + "not found!");
            } else if (!entity.canBeChildOf(parent.getType())) {
                result.setErrorMessage(type + " cannot be child of " + parent.getType());
            } else {
                entity.setName(name);
                entity.setType(type);
                entity.setValue(value);
                markTreeDirty(type);
                result = createMetadataResult(parent);
            }
        }
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "UPDATE", METADATA_KEY_TYPE, type.toString(), value, entity.toString()));
        return result;

    }

    // keep children for history/ recovery, delete only current field
    @Override
    public MetadataResponse deleteMetadataField(Long id) {
        MetadataResponse result = new MetadataResponse();
        MetadataEntity entity = entityMgr.find(MetadataEntity.class, id);
        entityMgr.remove(entity);
        if (entity.getParentId() != -1L) {
            MetadataField parent = getMetadataField(entity.getParentId());
            markTreeDirty(parent.getType());
            result = createMetadataResult(parent);
        }
        saveAuditEvent(new AuditEvent(DEFAULT_USER, "DELETE", METADATA_KEY_TYPE, entity.getType().toString(), entity.getValue(), entity.toString()));
        return result;
    }

    /*
     * given a Metadata field, create a MetadataResponse by looking up the
     * entire tree. set the input field as the result in the MetadataResponse.
     */
    private MetadataResponse createMetadataResult(MetadataField field) {
        MetadataResponse result = new MetadataResponse();
        if (field.getType().isSyncKeyType()) {
            result.setTree(getMetadataTree(MetadataType.Entities).getTree());
        } else {
            result.setTree(getMetadataTree(MetadataType.SearchKeys).getTree());
        }
        result.setResult(searchField(result.getTree(), field));
        return result;
    }

    private MetadataField getMetadataField(Long id) {
        MetadataEntity current = entityMgr.find(MetadataEntity.class, id);
        return (current == null) ? null : ConversionUtility.convertToMetadataField(current);
    }

    /*
     * DFS search
     */
    private MetadataField searchField(MetadataField tree, MetadataField field) {

        MetadataField result = null;
        if (tree != null && field != null) {
            if (tree.getId().equals(field.getId())) {
                return tree;
            } else {
                result = getMetadataField(tree, field);
            }
        }
        return result;
    }

    private MetadataField getMetadataField(MetadataField tree, MetadataField field) {

        for (MetadataField child : tree.getChildren()) {
            MetadataField dfsResult = searchField(child, field);
            if (dfsResult != null) {
                return dfsResult;
            }
        }
        return null;
    }

    @Override
    public MetadataResponse sync(String entity, String system, String key, String... values) {

        StringBuilder message = new StringBuilder("{");
        message.append("\"entity\" : \"");
        message.append(entity);
        message.append("\",");
        message.append("\"system\" : \"");
        message.append(system);
        message.append("\",");
        message.append("\"key\": \"");
        message.append(key);
        message.append("\",");
        message.append("\"values\" : [");

        int i = 0;
        for(String value: values) {
            if(value!=null && value.length()>0) {
                if(i>0) {
                    message.append(",");
                }
                message.append("\"");
                message.append(value);
                message.append("\"");
            }
            i++;
        }
        message.append("]");
        message.append("}");

        LOG.info("Initiating sync request: {}", message.toString());

        saveAuditEvent(new AuditEvent(DEFAULT_USER, "SYNC", METADATA_KEY_TYPE, entity, key, message.toString()));

        CloseableHttpClient httpClient;
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            for(String restEndPoint: getResyncRestEndpoints()) {
                try {
                    HttpPost httpPost = new HttpPost(restEndPoint);
                    httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    httpPost.setEntity(new StringEntity(message.toString()));

                    CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

                    if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
                        // status is Success by default
                        return new MetadataResponse();
                    } else {
                        // try another host
                        LOG.warn("unable to send resync message, received Http response code:" +
                                httpResponse.getStatusLine().getStatusCode() + " response message:" + httpResponse.getEntity().toString() + " from:" + restEndPoint);
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            httpClient.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        MetadataResponse result = new MetadataResponse();
        result.setErrorMessage("Unable to resync message");
        return result;
    }

    @Override
    public Map<String, List<String>> getSearchKeyValueSuggestions() {

        // ensure cache exists and is upto date.
        getMetadataTree(MetadataType.SearchKeys).getTree();
        return suggestionsCache;
    }

    private void updateSuggestions(MetadataField searchKeysTree) {

        Map<String, List<String>> newSuggestions = new HashMap<>();
        if(searchKeysTree!=null && !searchKeysTree.getChildren().isEmpty()) {
            for (MetadataField searchKey : searchKeysTree.getChildren()) {
                addSuggestion(newSuggestions, searchKey);
            }
        }
        suggestionsCache = newSuggestions;
    }

    private void addSuggestion(Map<String, List<String>> newSuggestions, MetadataField searchKey) {
        if (getSuggestedFields().contains(searchKey.getValue())) {
            List<String> values = new ArrayList<>();
            for (MetadataField suggestion : searchKey.getSuggestions()) {
                values.add(suggestion.getValue());
            }
            newSuggestions.put(searchKey.getValue(), values);
        } else {
            newSuggestions.put(searchKey.getValue(), null);
        }
    }

    public void ensureSuggestionsArePresent(EsbMessage message, Map<String, List<String>> extractedHeaders) {

        if(message.getHeaders()!=null) {
            for(Header header:message.getHeaders()) {
                if(getSuggestedFields().contains(header.getName())) {
                    ensureSuggestionIsPresent(header.getName(), header.getValue());
                }
            }
        }
        for(String suggestedField: getSuggestedFields()) {
            List<String> extractedValues = extractedHeaders.get(suggestedField);
            if(extractedValues!=null && !extractedValues.isEmpty()) {
                for(String extractedValue: extractedValues) {
                    ensureSuggestionIsPresent(suggestedField, extractedValue);
                }
            }
        }
    }

    // should be called only for fields defined as suggested fields
    private void ensureSuggestionIsPresent(String suggestedField, String suggestion) {
        if(!suggestionsCache.containsKey(suggestedField)) {
            Long parentId = treeCache.get(MetadataType.SearchKeys).getTree().getId();
            addChildMetadataField(parentId, suggestedField, MetadataType.SearchKey, suggestedField);
        }
        Long searchKeyId = null;
        if(!suggestionsCache.get(suggestedField).contains(suggestion)) {
            searchKeyId = fetchSearchKeyId(suggestedField);
            // fetch method can return null
            if(searchKeyId!=null) {
                addChildMetadataField(searchKeyId, suggestion, MetadataType.Suggestion, suggestion);
            } else {
                LOG.error("unable to add suggestion!");
            }
        }
    }

    private Long fetchSearchKeyId(String suggestedField) {
        Query query = entityMgr.createQuery("select f from MetadataEntity f where f.value = :value");
        query.setParameter("value", suggestedField);
        List<MetadataEntity> queryResult = (List<MetadataEntity>) query.getResultList();
        if (queryResult != null && !queryResult.isEmpty()) {
            return queryResult.get(0).getId();
        }
        return null;
    }

    private Boolean isEditableMessage( EsbMessage message ){
        return getEditableMessageTypes().contains( message.getMessageType() );
    }

}

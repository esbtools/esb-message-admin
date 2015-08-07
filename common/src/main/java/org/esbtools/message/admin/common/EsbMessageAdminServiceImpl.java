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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.esbtools.message.admin.Provider;
import org.esbtools.message.admin.common.dao.AuditEventDAO;
import org.esbtools.message.admin.common.dao.AuditEventDAOImpl;
import org.esbtools.message.admin.common.dao.EsbErrorDAO;
import org.esbtools.message.admin.common.dao.EsbErrorDAOImpl;
import org.esbtools.message.admin.common.dao.MetadataDAO;
import org.esbtools.message.admin.common.dao.MetadataDAOImpl;
import org.esbtools.message.admin.common.extractor.KeyExtractorException;
import org.esbtools.message.admin.common.extractor.KeyExtractorUtil;
import org.esbtools.message.admin.common.utility.EncryptionUtil;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Named
public class EsbMessageAdminServiceImpl implements Provider {

    private static final Logger LOG = Logger.getLogger(EsbMessageAdminServiceImpl.class.getName());
    private JSONObject config;
    private String encryptionKey;
    private transient EsbErrorDAO errorDao;
    private transient MetadataDAO metadataDao;
    private transient AuditEventDAO auditDao;
    private static transient KeyExtractorUtil extractor;
    private static transient EncryptionUtil encrypter;

    @Inject
    private EntityManager entityMgr;

    {
        try {
            InputStream configFile = this.getClass().getClassLoader().getResourceAsStream("config.json");
            JSONParser parser = new JSONParser();
            config = (JSONObject) parser.parse(new InputStreamReader(configFile, "UTF-8"));
            configFile.close();
            InputStream encryptionKeyFile = this.getClass().getClassLoader().getResourceAsStream("encryption.key");
            BufferedReader encryptionKeyFileReader = new BufferedReader(new InputStreamReader(encryptionKeyFile, "UTF-8"));
            encryptionKey = encryptionKeyFileReader.readLine();
            encryptionKeyFileReader.close();
            encryptionKeyFile.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EsbErrorDAO getErrorDAO() {
        return errorDao == null ? new EsbErrorDAOImpl(entityMgr, getAuditEventDAO(), getEncrypter(), config) : errorDao;
    }

    void setErrorEntityManager(EntityManager entityMgr) {
        this.entityMgr = entityMgr;
    }

    private MetadataDAO getMetadataDAO() {
        return metadataDao == null ? new MetadataDAOImpl(entityMgr, getAuditEventDAO(), config) : metadataDao;
    }

    private AuditEventDAO getAuditEventDAO() {
        return auditDao == null ? new AuditEventDAOImpl(entityMgr) : auditDao;
    }

    private KeyExtractorUtil getKeyExtractor() {

        MetadataResponse searchKeyResponse = getMetadataDAO().getMetadataTree(MetadataType.SearchKeys);
        if (extractor == null || !extractor.getHash().contentEquals(searchKeyResponse.getHash())) {
            List<MetadataField> searchKeys = (searchKeyResponse.getTree() != null) ? searchKeyResponse.getTree().getChildren() : new ArrayList<MetadataField>();
            extractor = new KeyExtractorUtil(searchKeys, searchKeyResponse.getHash());
        }
        return extractor;
    }

    private EncryptionUtil getEncrypter() {
        if (encrypter == null) {
            encrypter = new EncryptionUtil(encryptionKey);
        }
        return encrypter;
    }

    @Override
    public void persist(EsbMessage esbMessage) throws IOException {

        Map<String, List<String>> extractedHeaders = null;

        try {
            extractedHeaders = getKeyExtractor().getEntriesFromPayload(esbMessage.getPayload());
        } catch (KeyExtractorException e) {
            LOG.warning("Could not extract metadata! " + e);
            extractedHeaders = new HashMap<>();
        }

        getErrorDAO().create(esbMessage, extractedHeaders);
        getMetadataDAO().ensureSuggestionsArePresent(esbMessage, extractedHeaders);

    }

    @Override
    public void persist(EsbMessage[] esbMessages) throws IOException {
        for (EsbMessage esbMessage:esbMessages) {
            persist(esbMessage);
        }
    }

    @Override
    public SearchResult searchMessagesByCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, boolean sortAsc, int start, int maxResults) {

        if (fromDate == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            // TODO get magic number from a property file
            c.add(Calendar.DATE, -30);
            fromDate = c.getTime();
        }
        if (toDate == null) {
            toDate = new Date();
        }
        return getErrorDAO().findMessagesBySearchCriteria(criteria, fromDate, toDate, sortField, sortAsc, start, maxResults);
    }

    @Override
    public SearchResult getMessageById(Long id) {
        return getErrorDAO().getMessageById(id);
    }

    @Override
    public Map<String, List<String>> getSearchKeyValueSuggestions() {
        return getMetadataDAO().getSearchKeyValueSuggestions();
    }

    @Override
    public MetadataResponse getMetadataTree(MetadataType type) {
        return getMetadataDAO().getMetadataTree(type);
    }

    @Override
    public MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value) {
        return getMetadataDAO().addChildMetadataField(parentId, name, type, value);
    }

    @Override
    public MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value) {
        return getMetadataDAO().updateMetadataField(id, name, type, value);
    }

    @Override
    public MetadataResponse deleteMetadataField(Long id) {
        return getMetadataDAO().deleteMetadataField(id);
    }

    @Override
    public MetadataResponse sync(String entity, String system, String key, String... values) {
        return getMetadataDAO().sync(entity, system, key, values);

    }

}

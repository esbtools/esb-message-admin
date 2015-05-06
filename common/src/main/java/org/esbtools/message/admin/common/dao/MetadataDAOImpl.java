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

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.esbtools.message.admin.common.ConversionUtility;
import org.esbtools.message.admin.common.orm.MetadataEntity;
import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MetadataDAOImpl implements MetadataDAO {

    private final EntityManager mgr;
    private final AuditEventDAO auditDAO;
    private final static Logger log = Logger.getLogger(MetadataDAOImpl.class.getName());
    private static transient Map<MetadataType, MetadataResponse> treeCache = new HashMap<>();
    private static transient Map<String, List<String>> suggestionsCache = new HashMap<>();

    private Set<String> suggestedFields = new HashSet<>();
    private List<String> resyncRestEndpoints = new ArrayList<>();

    public MetadataDAOImpl(EntityManager mgr, AuditEventDAO auditDAO, JSONObject config) {
        this.mgr=mgr;
        this.auditDAO = auditDAO;
        JSONArray suggestConfigs = (JSONArray) config.get("suggestedFields");
        if(suggestConfigs!=null) {
            for(int i=0;i<suggestConfigs.size();i++) {
                suggestedFields.add(suggestConfigs.get(i).toString());
            }
        }
        JSONArray resyncEndPoints = (JSONArray) config.get("resyncRestEndpoints");
        if(resyncEndPoints!=null) {
            for(Object endPoint: resyncEndPoints) {
                resyncRestEndpoints.add(endPoint.toString());
            }
        }
        if(resyncRestEndpoints.size()<1) {
            throw new IllegalStateException("at least one resync rest end point needs to be configured");
        }
    }

    private String getMetadataHash(MetadataType type) {
        String hash = "";
        if(type==MetadataType.SearchKeys || type==MetadataType.Entities) {
            Query query = mgr.createQuery("select f from MetadataEntity f where f.type = '" +type+"'");
            List<MetadataEntity> result = query.getResultList();
            if(result!=null && result.size()>0) {
                hash = (String) result.get(0).getValue();
            }
        }
        return hash;
    }

    private String markTreeDirty(MetadataType type) {
        String hash = null;
        if(type.isSearchKeyType()) {
            type = MetadataType.SearchKeys;
        } else {
            type = MetadataType.Entities;
        }
        Query query = mgr.createQuery("select f from MetadataEntity f where f.type = '" +type+"'");
        List<MetadataEntity> result = query.getResultList();
        if(result!=null && result.size()>0) {
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
                result = new MetadataResponse();
                String inClause = null;
                if (type == MetadataType.Entities) {
                    inClause = "('Entities', 'Entity', 'System', 'SyncKey')";
                } else {
                    inClause = "('SearchKeys', 'SearchKey', 'XPATH', 'Suggestion')";
                }
                if (inClause != null) {
                    Query query = mgr.createQuery("select f from MetadataEntity f where f.type in " + inClause);
                    List<MetadataEntity> queryResult = (List<MetadataEntity>) query.getResultList();
                    result.setTree(makeTree(queryResult));
                    result.setHash(hash);
                    treeCache.put(type, result);
                    if(type == MetadataType.SearchKeys) {
                        updateSuggestions(result.getTree());
                    }
                }
            }
        } else {
            result = new MetadataResponse();
            result.setErrorMessage("Illegal Argument:" + type + ", Expected: Entities or SearchKeys");
        }
        return result;
    }

    /*
     * given a list of metadata entities with parent ids, create a tree of
     * Metadafields.
     */
    private static MetadataField makeTree(List<MetadataEntity> entities) {
        MetadataField root = null;
        Map<Long, MetadataField> map = new HashMap<Long, MetadataField>();
        int i = 0;
        for (MetadataEntity entity : entities) {
            i++;
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
            if (entity.getParentId().intValue() != -1 && (parent=map.get(entity.getParentId()))!= null) {
                parent.addDescendant((field));
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
                result.setErrorMessage("Illegal Argument:" + type + ", If parent = -1, Expected: Entities or SearchKeys");
            } else {
                markTreeDirty(type);
                mgr.persist(curr);
                result = getMetadataTree(type);
            }
        } else {
            MetadataField parent = getMetadataField(parentId);
            if(parent==null) {
                result.setErrorMessage("Illegal Argument:parent "+parentId+ " not found!");
            } else if (!curr.canBeChildOf(parent.getType())) {
                result.setErrorMessage("Illegal Argument: " + type + " can not be a child of " + parent.getType());
            } else {
                markTreeDirty(type);
                mgr.persist(curr);
                result = createMetadataResult(parent);
            }
        }
        auditDAO.save("someUser", "ADD", "metadata", type.toString(), value, curr.toString());
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
        MetadataEntity entity = mgr.find(MetadataEntity.class, id);

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
        auditDAO.save("someUser", "UPDATE", "metadata", type.toString(), value, entity.toString());
        return result;

    }

    // keep children for history/ recovery, delete only current field
    @Override
    public MetadataResponse deleteMetadataField(Long id) {
        MetadataResponse result = new MetadataResponse();
        MetadataEntity entity = mgr.find(MetadataEntity.class, id);
        mgr.remove(entity);
        if (entity.getParentId() != -1L) {
            MetadataField parent = getMetadataField(entity.getParentId());
            markTreeDirty(parent.getType());
            result = createMetadataResult(parent);
        }
        auditDAO.save("someUser", "DELETE", "metadata", entity.getType().toString(), entity.getValue(), entity.toString());
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

        // if it is a top level field, return null;
        MetadataEntity current = null;
        current = mgr.find(MetadataEntity.class, id);
        if (current != null) {
            return ConversionUtility.convertToMetadataField(current);
        }
        return null;
    }

    /*
     * DFS search
     */
    private MetadataField searchField(MetadataField tree, MetadataField field) {

        MetadataField result = null;
        if (tree != null && field != null) {
            if (tree.getId() == field.getId()) {
                return tree;
            } else {
                for (MetadataField child : tree.getChildren()) {
                    MetadataField dfsResult = searchField(child, field);
                    if (dfsResult != null) {
                        return dfsResult;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public MetadataResponse sync(String entity, String system, String key, String... values) {

        // create JMS Payload
        StringBuilder message = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        message.append("<SyncRequest><EntityName>");
        message.append(entity);
        message.append("</EntityName><System>");
        message.append(system);
        message.append("</System><KeyName>");
        message.append(key);
        message.append("</KeyName>");
        for(String value: values) {
            if(value!=null && value.length()>0) {
                message.append("<KeyValue>");
                message.append(value);
                message.append("</KeyValue>");
            }
        }
        message.append("</SyncRequest>");
        log.log(Level.INFO, "Initiating sync request:"+message.toString());

        auditDAO.save("someUser", "SYNC", "metadata", entity, key, message.toString());

        boolean foundActiveHost = false;
        for(String restEndPoint: resyncRestEndpoints) {

            try {
                URL url = new URL(restEndPoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/xml");
                OutputStream os = conn.getOutputStream();
                os.write(message.toString().getBytes());
                os.flush();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // status is Success by default
                    return new MetadataResponse();
                } else {
                    // try another host
                    log.warning("unable to send resync message, recieved Http response code:"+
                            conn.getResponseCode()+ " response message:"+conn.getResponseMessage()+" from:"+restEndPoint);
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                log.severe(e.getMessage());
            } catch (IOException e) {
                log.severe(e.getMessage());
            }
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

        Map<String, List<String>> newSuggestions = new HashMap<String, List<String>>();
        if(searchKeysTree!=null && searchKeysTree.getChildren().size()>0) {
            for (MetadataField searchKey : searchKeysTree.getChildren()) {
                if (suggestedFields.contains(searchKey.getValue())) {
                    List<String> values = new ArrayList<String>();
                    for (MetadataField suggestion : searchKey.getSuggestions()) {
                        values.add(suggestion.getValue());
                    }
                    newSuggestions.put(searchKey.getValue(), values);
                } else {
                    newSuggestions.put(searchKey.getValue(), null);
                }
            }
        }
        suggestionsCache = newSuggestions;
    }

    @Override
    public void ensureSuggestionsArePresent(Map<String, List<String>> extractedHeaders) {

        for(String suggestedField: suggestedFields) {

            List<String> extractedValues = extractedHeaders.get(suggestedField);
            if(extractedValues!=null && extractedValues.size()>0) {
                if(!suggestionsCache.containsKey(suggestedField)) {
                    Long parentId = treeCache.get(MetadataType.SearchKeys).getTree().getId();
                    addChildMetadataField(parentId, suggestedField, MetadataType.SearchKey, suggestedField);
                }
                for(String extractedValue: extractedValues) {
                    Long searchKeyId = null;
                    if(!suggestionsCache.get(suggestedField).contains(extractedValue)) {
                        if(searchKeyId==null) {
                            searchKeyId = fetchSearchKeyId(suggestedField);
                        }
                        // fetch method can return null
                        if(searchKeyId!=null) {
                            addChildMetadataField(searchKeyId, extractedValue, MetadataType.Suggestion, extractedValue);
                        } else {
                            log.severe("unable to find search key to add suggestion!");
                        }
                    }
                }
            }
        }
    }

    private Long fetchSearchKeyId(String suggestedField) {
        Query query = mgr.createQuery("select f from MetadataEntity f where f.value = :value");
        query.setParameter("value", suggestedField);
        List<MetadataEntity> queryResult = (List<MetadataEntity>) query.getResultList();
        if (queryResult != null && queryResult.size() != 0) {
            return queryResult.get(0).getId();
        }
        return null;
    }

}

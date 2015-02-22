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

import java.util.List;
import java.util.Map;

import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;



public interface MetadataDAO {

    /**
     * Returns entire tree given the tree type = Entities / SearchKeys
     */
    public MetadataResponse getMetadataTree(MetadataType type);

    /**
     * Add a child field
     */
    public MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value);

    /**
     * update an existing field
     */
    public MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value);

    /**
     * delete a field
     */
    public MetadataResponse deleteMetadataField(Long id);

    /**
     * fetch suggestions for search keys and values ( for some of the keys )
     */
    public Map<String, List<String>> getSearchKeyValueSuggestions();

    /**
     * sync an entity by enqueuing a JMS request
     */
    public void sync(String entity, String system, String key, String... values);

}

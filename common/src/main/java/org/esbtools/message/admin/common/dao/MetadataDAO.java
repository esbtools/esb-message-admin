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

import java.util.List;
import java.util.Map;

import org.esbtools.message.admin.model.EsbMessage;
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
    public MetadataResponse sync(String entity, String system, String key, String... values);

    /*
     * ensure all new suggestions are set for specific keys
     */
    public void ensureSuggestionsArePresent(EsbMessage esbMessage, Map<String, List<String>> extractedHeaders);

}

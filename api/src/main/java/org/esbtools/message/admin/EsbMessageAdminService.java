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
package org.esbtools.message.admin;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.MetadataResponse;
import org.esbtools.message.admin.model.MetadataType;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;
import org.esbtools.message.admin.model.MessageSearchConfigurations;

public interface EsbMessageAdminService {

    /**
     * Persists a single ESB Message
     *
     * @param esbMessage an ESB Message
     * @throws IOException
     */
    void persist(EsbMessage esbMessage) throws IOException;

    /**
     * Persists multiple ESB MessagesA
     *
     * @param esbMessages array of ESB Messages
     * @throws IOException
     */
    void persist(EsbMessage[] esbMessages) throws IOException;

    /**
     * Updates a given ESB Message ( Payload only )
     *
     * @param messageId - a message Id
     * @param messageBody - the message body
     * @throws IOException
     */
    MetadataResponse resubmit(Long messageId, String messageBody);

    /**
     * @param criteria          search
     * @param fromDate          the start timestamp of the range
     * @param toDate            the end timestamp of the range
     * @param start             sets the position of the first result to retrieve
     * @param maxResults        sets the maximum number of results to retrieve
     * @return SearchResult     results matching the search criteria
     */
    SearchResult searchMessagesByCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, boolean sortAsc, int start, int maxResults);

    /**
     * Returns details for a specific message
     *
     * @param id                the id of the message to retrieve
     * @return SearchResult     the resulting message
     */
    SearchResult getMessageById(Long id);

    /**
     * Suggests search key and value suggestions
     *
     * @return all key and value suggestions
     */
    Map<String, List<String>> getSearchKeyValueSuggestions();

    /**
     * @param type       specific tree type to return, possible values:
     *                   [Entities,KeyGroups]
     * @return MetadataResponse the entire keys tree
     */
    MetadataResponse getMetadataTree(MetadataType type);

    /**
     * @param parentId          the id of the MetadataField to add a child to
     * @param name              the name of the child
     * @param type              the type of the child
     * @param value             the value of the child
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value);

    /**
     * @param id                the id of the MetadataField to update
     * @param name              the name of the field
     * @param type              the type of the field
     * @param value             the value of the field
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value);

    /**
     * @param id                the id of the MetadataField to delete
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    MetadataResponse deleteMetadataField(Long id);

    /**
     * @param entity            the entity to sync
     * @param system            the system to sync from
     * @param key               the key name using which to sync
     * @param values            the values of the key
     */
    MetadataResponse sync(String entity, String system, String key, String... values);

    /**
     * Fetches configurations used for searching messages from the configuration store
     *
     * @return MessageSearchConfigurations an object containing all pertinent configurations
     */
    MessageSearchConfigurations getSearchConfigurations();
}

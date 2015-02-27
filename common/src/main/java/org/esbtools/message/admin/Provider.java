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
public interface Provider {

    /**
     * Persists a single ESB Message
     *
     * @param esbMessage an ESB Message
     * @throws IOException
     */
    public void persist(EsbMessage esbMessage) throws IOException;

    /**
     * Persists multiple ESB MessagesA
     *
     * @param esbMessages array of ESB Messages
     * @throws IOException
     */
    public void persist(EsbMessage[] esbMessages) throws IOException;

    /**
     * @param criteria          search
     * @param fromDate          the start timestamp of the range
     * @param toDate            the end timestamp of the range
     * @param start             sets the position of the first result to retrieve
     * @param maxResults        sets the maximum number of results to retrieve
     * @return SearchResult     results matching the search criteria
     */
    public SearchResult searchMessagesByCriteria(SearchCriteria criteria, Date fromDate, Date toDate, int start, int maxResults);

    /**
     * Returns details for a specific message
     *
     * @param id                the id of the message to retrieve
     * @return SearchResult     the resulting message
     */
    public SearchResult getMessageById(Long id);

    /**
     * Suggests search key and value suggestions
     *
     * @return all key and value suggestions
     */
    public Map<String, List<String>> getSearchKeyValueSuggestions();

    /**
     * @param type       specific tree type to return, possible values:
     *                   [Entities,KeyGroups]
     * @return MetadataResponse the entire keys tree
     */
    public MetadataResponse getMetadataTree(MetadataType type);

    /**
     * @param parentId          the id of the MetadataField to add a child to
     * @param name              the name of the child
     * @param type              the type of the child
     * @param value             the value of the child
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    public MetadataResponse addChildMetadataField(Long parentId, String name, MetadataType type, String value);

    /**
     * @param id                the id of the MetadataField to update
     * @param name              the name of the field
     * @param type              the type of the field
     * @param value             the value of the field
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    public MetadataResponse updateMetadataField(Long id, String name, MetadataType type, String value);

    /**
     * @param id                the id of the MetadataField to delete
     * @return MetadataResponse the entire MetadataField tree and the parent field
     */
    public MetadataResponse deleteMetadataField(Long id);

    /**
     * @param entity            the entity to sync
     * @param system            the system to sync from
     * @param key               the key name using which to sync
     * @param values            the values of the key
     */
    public void sync(String entity, String system, String key, String... values);

}

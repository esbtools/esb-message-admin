package org.esbtools.message.admin.common.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.model.SearchCriteria;
import org.esbtools.message.admin.model.SearchResult;



public interface EsbErrorDAO {

    /**
     * Creates a new EsbError entity
     */
    public void create(EsbMessageEntity ee, Map<String, List<String>> extractedHeaders);

    /**
     * Returns error message of the given queue
     */
    public SearchResult findMessagesBySearchCriteria(SearchCriteria criteria, Date fromDate, Date toDate, String sortField, Boolean sortAsc, Integer start, Integer maxResults);

    /**
     * Returns the error message given the id
     */
    public SearchResult getMessageById(Long id);

}

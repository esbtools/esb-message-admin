package org.esbtools.message.admin.common.dao.generic;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Generic Repository, providing basic CRUD operations
 *
 * @param <T> the entity type
 * @param <ID> the primary key type
 *
 * @author ykoer
 */
public interface GenericDAO<T, ID extends Serializable> {

    /**
     * Find an entity by its primary key
     *
     * @param id
     *            the primary key
     * @return
     *            the entity
     */
    T findById(final ID id);

    /**
     * Load all entities.
     *
     * @return
     *            the list of entities
     */
    List<T> findAll();

    /**
     * Find using a named query.
     *
     * @param queryName
     *            the name of the query
     * @param params
     *            the query parameters
     *
     * @return
     *            the list of entities
     */
    List<T> findByNamedQuery(
        final String queryName,
        Object... params
    );

    /**
     * Find using a named query.
     *
     * @param queryName
     *            the name of the query
     * @param params
     *            the query parameters
     *
     * @return
     *            the list of entities
     */
    List<T> findByNamedQueryAndNamedParams(
        final String queryName,
        final Map<String, ?extends Object> params
    );

    /**
     * save an entity. This can be either a INSERT or UPDATE in the database.
     *
     * @param entity
     *            the entity to save
     *
     * @return
     *            the saved entity
     */
    T save(final T entity);

    /**
     * delete an entity from the database.
     *
     * @param entity
     *            the entity to delete
     */
    void delete(final T entity);

    /**
     * delete all entities from the database.
     */
    <T> void deleteAll();

    /**
     * count items by the given persistence class and the predicates
     *
     * @param clazz
     *            the persistence class
     * @param predicates
     *            the criteria predicate array
     * @return
     *            total number of persistent entities matching the criteria
     */
    <T> Long countItems(Class<T> clazz, Predicate[] predicates);

    /**
     * count items by the given root type in the from clause, the query and the predicates
     * @param from
     *            the root type in the from clause
     * @param totalQuery
     *            the query
     * @param predicates
     *            the criteria predicate array
     * @return
     *            total number of persistent entities matching the criteria
     */
    <T> Long countItems(Root<T> from, CriteriaQuery<Long> totalQuery, Predicate[] predicates);

    /**
     * find items by the given root, the query and the predicates
     *
     * @param from
     *            the root type in the from clause
     * @param itemQuery
     *            the query
     * @param predicates
     *            the criteria predicate array
     * @param firstResult
     *            the first result index
     * @param maxResults
     *            number of results to retrieve
     * @param sortField
     *            field name to use for sorting
     * @param direction
     *            ascending(ASC) or descending(DESC) sorting
     * @return
     *            list of persistent entities matching the criteria
     */
    <T> List<T> findItems(Root<T> from,
                          CriteriaQuery<T> itemQuery,
                          Predicate[] predicates,
                          Integer firstResult,
                          Integer maxResults,
                          String sortField,
                          String direction);
}

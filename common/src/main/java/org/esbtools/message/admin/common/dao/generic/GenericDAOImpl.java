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
package org.esbtools.message.admin.common.dao.generic;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * JPA implementation of a Generic DAO.
 *
 * @author ykoer
 *
 * @param <T>
 *            The persistent type
 * @param <ID>
 *            The primary key type
 */
public class GenericDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {

    // ~ Instance fields
    // --------------------------------------------------------

    public final Class<T> persistentClass;
    private EntityManager entityManager;

    // ~ Constructors
    // -----------------------------------------------------------

    @SuppressWarnings("unchecked")
    public GenericDAOImpl() {

        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public GenericDAOImpl(final Class<T> persistentClass) {
        super();
        this.persistentClass = persistentClass;
    }

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * set the JPA entity manager to use.
     *
     * @param entityManager
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#findAll()
     */
    @Override
    public List<T> findAll() {

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(persistentClass);
        Root<T> from = criteriaQuery.from(persistentClass);

        criteriaQuery.select(from);
        TypedQuery<T> itemTypedQuery = getEntityManager().createQuery(criteriaQuery);

         return itemTypedQuery.getResultList();
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#findById(java.io.Serializable)
     */
    @Override
    public T findById(final ID id) {
        final T result = getEntityManager().find(persistentClass, id);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#findByNamedQuery(java.lang.String, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByNamedQuery(final String name, Object... params) {
        Query query = getEntityManager().createNamedQuery(name);

        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }

        final List<T> result = (List<T>) query.getResultList();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#findByNamedQueryAndNamedParams(java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> findByNamedQueryAndNamedParams(final String name,
            final Map<String, ? extends Object> params) {
        javax.persistence.Query query = getEntityManager().createNamedQuery(
                name);

        for (final Map.Entry<String, ? extends Object> param : params
                .entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        final List<T> result = (List<T>) query.getResultList();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#delete(java.lang.Object)
     */
    @Override
    public void delete(T entity) {
        getEntityManager().remove(entity);
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#delete(java.lang.Object)
     */
    @Override
    public void deleteAll() {

        // JPA 2.0 doesn't support CriteriaDelete. So need to fall back to normal JPQL
        int count = getEntityManager().createQuery("DELETE FROM " + persistentClass.getSimpleName()).executeUpdate();
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#save(java.lang.Object)
     */
    @Override
    public T save(T entity) {
        final T savedEntity = getEntityManager().merge(entity);
        return savedEntity;
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#countItems(java.lang.Class, javax.persistence.criteria.Predicate[])
     */
    @Override
    public <T> Long countItems(Class<T> clazz, Predicate[] predicates) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> totalQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> from = totalQuery.from(clazz);
        totalQuery.select(criteriaBuilder.count(from));
        return countItems(from, totalQuery, predicates);
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#countItems(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.Predicate[])
     */
    @Override
    public <T> Long countItems(Root<T> from, CriteriaQuery<Long> totalQuery, Predicate[] predicates) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        totalQuery.select(criteriaBuilder.count(from));
        if ((predicates != null) && (predicates.length > 0) ) {
            totalQuery.where(predicates);
        }
        TypedQuery<Long> longTypedQuery = getEntityManager().createQuery(totalQuery);
        return longTypedQuery.getSingleResult();
    }

    /*
     * (non-Javadoc)
     * @see org.esbtools.message.admin.service.dao.generic.GenericDAO#findItems(javax.persistence.criteria.Root, javax.persistence.criteria.CriteriaQuery, javax.persistence.criteria.Predicate[], java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public <T> List<T> findItems(Root<T> from,
                                 CriteriaQuery<T> itemQuery,
                                 Predicate[] predicates,
                                 Integer firstResult,
                                 Integer maxResults,
                                 String sortField,
                                 String direction) {
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            List<Order> orders = new ArrayList<Order>();

            if (sortField != null) {
                orders.add("DESC".equals(direction) ? criteriaBuilder.desc(from.get(sortField)) : criteriaBuilder.asc(from.get(sortField)));
            }

            if ((predicates != null) && (predicates.length > 0)) {
                itemQuery.select(from).where(predicates).orderBy(orders);
            } else {
                itemQuery.select(from).orderBy(orders);
            }

            TypedQuery<T> itemTypedQuery = getEntityManager().createQuery(itemQuery);
            if (firstResult == null || firstResult < 0) {
                firstResult = 0;
            }
            if (maxResults == null) {
                maxResults = 100;
            }

            return itemTypedQuery.setFirstResult(firstResult)
                    .setMaxResults(maxResults).getResultList();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

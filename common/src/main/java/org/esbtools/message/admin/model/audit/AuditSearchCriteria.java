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
package org.esbtools.message.admin.model.audit;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Instances of AuditSearchCriteria are used to search audit events.
 * SearchCriteria encapsulates an array of AuditCriterion objects.
 *
 * @author ykoer
 */

@JsonIgnoreProperties(ignoreUnknown = true) //Allow fields to be added to model without breaking clients
public class AuditSearchCriteria implements Serializable {

    public enum Direction {
        ASC,DESC;
    }

    private static final long serialVersionUID=1l;

    private AuditCriterion[] criteria;
    private AuditCriterion.Field sortField;
    private Direction direction;
    private int firstResultIndex;
    private int maxResults;

    /**
     * Creates an empty search criteria
     */
    public AuditSearchCriteria() {
    }

    /**
     * Creates a search criteria with a single criterion
     * with the given criterion
     */
    public AuditSearchCriteria(AuditCriterion criterion) {
        this(new AuditCriterion[] {criterion});
    }

    /**
     * Creates a search criteria with multiple criterion
     *
     * @param criteria The audit search criteria
     */
    public AuditSearchCriteria(AuditCriterion[] criteria) {
        this.criteria=criteria;
    }

    /**
     * The search criteria
     */
    public AuditCriterion[] getCriteria() {
        return criteria;
    }

    /**
     * The search criteria
     */
    public void setCriteria(AuditCriterion[] criteria) {
        this.criteria=criteria;
    }

    /**
     * Sets the search criteria using the criterion
     */
    public void setCriteria(AuditCriterion criterion) {
        this.criteria=new AuditCriterion[] {criterion};
    }

    public String toString() {
        return AuditCriterion.toString(criteria);
    }

    public AuditCriterion.Field getSortField() {
        return sortField;
    }

    public void setSortField(AuditCriterion.Field sortField) {
        this.sortField = sortField;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getFirstResultIndex() {
        return firstResultIndex;
    }

    public void setFirstResultIndex(int firstResultIndex) {
        this.firstResultIndex = firstResultIndex;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
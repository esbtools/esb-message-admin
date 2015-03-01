package org.esbtools.message.admin.model;

import java.io.Serializable;

public class SearchCriteria implements Serializable {

    private static final long serialVersionUID = 4766130222926946325L;
    private Criterion[] criteria;

    /**
     * Default constructor, constructs an empty search criteria
     */
    public SearchCriteria() {
    }

    /**
     * Constructs a search criteria to search for a string value in a field
     *
     * @param field One of the user fields
     * @param value Field value to search for
     * @param type Search type
     *
     * If the field value contains SQL wildcards (% and _), they are escaped.
     */
    public SearchCriteria(SearchField argField, String argValue) {
        Criterion criterion = new Criterion(argField, argValue);
        criteria = new Criterion[1];
        criteria[0] = criterion;
    }

    /**
     * Constructs a search criteria to search for a Long value (e.g. ACCOUNT_NUMBER)
     *
     * @param field Field the search
     * @param value Field value to search
     *
     * This ctor always constructs an EXACT_MATCH search.
     */
    public SearchCriteria(SearchField argField, Long argValue) {
        Criterion criterion = new Criterion(argField, argValue);
        criteria = new Criterion[1];
        criteria[0] = criterion;
    }

    /**
     * Constructs a criteria to search for a generic value (e.g. ORG_ADMIN)
     */
    public SearchCriteria(SearchField argField) {
        Criterion criterion = new Criterion(argField, null);
        criteria = new Criterion[1];
        criteria[0] = criterion;
    }

    /**
     * Constructs a criteria to search for an array of Criterion
     */
    public SearchCriteria(Criterion[] criteria) {
        this.setCriteria(criteria);
    }

    /**
     * @return the criteria
     */
    public Criterion[] getCriteria() {
        return criteria;
    }

    /**
     * @param criteria the criteria to set
     */
    public void setCriteria(Criterion[] criteria) {
        this.criteria = criteria;
    }

    public String toString() {
        StringBuilder text = new StringBuilder("SearchCriteria:{");
        for (Criterion criterion : criteria)
            text.append(criterion.toString());
        return text.append("}").toString();
    }

}

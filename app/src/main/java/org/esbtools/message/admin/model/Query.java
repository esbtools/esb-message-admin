package org.esbtools.message.admin.model;

import java.util.Date;
import java.util.List;

public class Query {

    private int firstResult;
    private int maxResults;

    private Date dateFrom;
    private Date dateTo;

    private String queueName;

    private List<Term> terms;

    public int getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    @Override
    public String toString() {
        return "Query [firstResult=" + firstResult + ", maxResults="
                + maxResults + ", dateFrom=" + dateFrom + ", dateTo=" + dateTo
                + ", queueName=" + queueName + ", terms=" + terms + "]";
    }
}

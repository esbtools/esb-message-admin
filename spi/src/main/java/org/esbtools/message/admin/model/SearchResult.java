package org.esbtools.message.admin.model;

public class SearchResult {

    private long totalResults;
    private int itemsPerPage;
    private int page;


    private EsbMessage[] messages;

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long totalResults) {
        this.totalResults = totalResults;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public EsbMessage[] getMessages() {
        return messages;
    }

    public void setMessages(EsbMessage[] messages) {
        this.messages = messages;
    }

    public static SearchResult empty() {
        SearchResult s = new SearchResult();
        s.setTotalResults(0);
        return s;
    }
}

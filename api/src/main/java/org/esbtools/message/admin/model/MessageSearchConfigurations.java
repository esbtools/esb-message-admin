package org.esbtools.message.admin.model;

import java.util.List;

public class MessageSearchConfigurations {
    private List<MessageSearchConfiguration> searchSystems;
    private List<MessageSearchConfiguration> searchEntities;
    private List<MessageSearchConfiguration> searchFilters;
    private String searchSystemKey;
    private String searchEntityKey;

    public List<MessageSearchConfiguration> getSearchSystems() {
        return searchSystems;
    }

    public void setSearchSystems(List<MessageSearchConfiguration> searchSystems) {
        this.searchSystems = searchSystems;
    }

    public List<MessageSearchConfiguration> getSearchEntities() {
        return searchEntities;
    }

    public void setSearchEntities(List<MessageSearchConfiguration> searchEntities) {
        this.searchEntities = searchEntities;
    }

    public List<MessageSearchConfiguration> getSearchFilters() {
        return searchFilters;
    }

    public void setSearchFilters(List<MessageSearchConfiguration> searchFilters) {
        this.searchFilters = searchFilters;
    }

    public String getSearchSystemKey() {
        return searchSystemKey;
    }

    public void setSearchSystemKey(String searchSystemKey) {
        this.searchSystemKey = searchSystemKey;
    }

    public String getSearchEntityKey() {
        return searchEntityKey;
    }

    public void setSearchEntityKey(String searchEntityKey) {
        this.searchEntityKey = searchEntityKey;
    }
}

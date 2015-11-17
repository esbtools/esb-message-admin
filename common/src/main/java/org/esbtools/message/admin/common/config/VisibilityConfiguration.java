package org.esbtools.message.admin.common.config;

import java.util.Map;

/**
 * Created by dhaynes on 9/3/15.
 */
public class VisibilityConfiguration {

    private Map<String,String> matchCriteriaMap = null;
    private Map<String,String> configurationMap = null;

    public Map<String,String> getMatchCriteriaMap() {
        return matchCriteriaMap;
    }
    public void setMatchCriteriaMap(Map<String,String> matchCriteriaMap) {
        this.matchCriteriaMap = matchCriteriaMap;
    }
    public Map<String,String> getConfigurationMap() {
        return configurationMap;
    }
    public void setConfigurationMap(Map<String,String> configurationMap) {
        this.configurationMap = configurationMap;
    }
}

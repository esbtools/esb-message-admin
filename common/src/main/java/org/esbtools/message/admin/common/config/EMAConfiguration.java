package org.esbtools.message.admin.common.config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EMAConfiguration {

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static JSONObject jsonConfig;
    private static String encryptionKey;
    private static Set<String> sortingFields;
    private static Set<String> suggestedFields;
    private static List<String> resyncRestEndpoints;
    private static List<VisibilityConfiguration> nonViewableMessages;
    private static List<VisibilityConfiguration> partiallyViewableMessages;
    private static List<String> editableMessageTypes;
    private static List<String> resubmitBlackList;
    private static List<String> resubmitRestEndpoints;

    private EMAConfiguration() {

    }

    public static JSONObject getJsonConfig() {
        if (null == jsonConfig) {
            jsonConfig = loadJsonConfiguration();
        }
        return jsonConfig;
    }

    public static String getEncryptionKey() {
        if (null == encryptionKey) {
            encryptionKey = loadEncryptionKey();
        }
        return encryptionKey;
    }

    public static synchronized Set<String> getSortingFields() {
        if (null == sortingFields) {
            sortingFields = loadSortingFields();
        }
        return sortingFields;
    }

    public static synchronized Set<String> getSuggestedFields() {
        if (null == suggestedFields) {
            suggestedFields = loadSuggestedFields();
        }
        return suggestedFields;
    }

    public static synchronized List<String> getResyncRestEndpoints() {
        if (null == resyncRestEndpoints) {
            resyncRestEndpoints = loadResyncRestEndpoints();
        }
        return resyncRestEndpoints;
    }

    public static synchronized List<String> getEditableMessageTypes() {
        if (null == editableMessageTypes) {
            editableMessageTypes = loadEditableMessageTypes();
        }
        return editableMessageTypes;
    }

    public static synchronized List<String> getResubmitBlackList() {
        if (null == resubmitBlackList) {
            resubmitBlackList = loadResubmitBlackList();
        }
        return resubmitBlackList;
    }

    public static synchronized List<String> getResubmitRestEndpoints() {
        if ( null == resubmitRestEndpoints ) {
            resubmitRestEndpoints = loadResubmitRestEndpoints();
        }
        return resubmitRestEndpoints;
    }

    public static List<VisibilityConfiguration> getNonViewableMessages() {
        if (null == nonViewableMessages) {
            nonViewableMessages = loadNonViewableConfiguration();
        }
        return nonViewableMessages;
    }

    public static List<VisibilityConfiguration> getPartiallyViewableMessages() {
        if (null == partiallyViewableMessages) {
            partiallyViewableMessages = loadPartiallyViewableConfiguration();
        }
        return partiallyViewableMessages;
    }

    private static JSONObject loadJsonConfiguration() {
        JSONObject jsonObject;
        try {
            InputStream configFile = EMAConfiguration.class.getClassLoader().getResourceAsStream("config.json");
            JSONParser parser = new JSONParser();
            jsonObject = (JSONObject) parser.parse(new InputStreamReader(configFile, DEFAULT_ENCODING));
            configFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load configuration file", e);
        }
        return jsonObject;
    }

    private static String loadEncryptionKey() {
        String encryptionKey;

        try {
            InputStream encryptionKeyFile = EMAConfiguration.class.getClassLoader().getResourceAsStream("encryption.key");
            BufferedReader encryptionKeyFileReader = new BufferedReader(new InputStreamReader(encryptionKeyFile, DEFAULT_ENCODING));
            encryptionKey = encryptionKeyFileReader.readLine();
            encryptionKeyFileReader.close();
            encryptionKeyFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load encryption key", e);
        }

        return encryptionKey;
    }

    private static Set<String> loadSortingFields() {
        Set<String> sortingFields = new HashSet<>();
        JSONArray sortFields = (JSONArray) getJsonConfig().get("sortingFields");
        if(sortFields!=null) {
            for(int i=0;i<sortFields.size();i++) {
                sortingFields.add(sortFields.get(i).toString());
            }
        }
        return sortingFields;
    }

    private static Set<String> loadSuggestedFields() {
        Set<String> suggestedFields = new HashSet<>();
        JSONArray suggestConfigs = (JSONArray) getJsonConfig().get("suggestedFields");
        if(suggestConfigs!=null) {
            for(int i=0;i<suggestConfigs.size();i++) {
                suggestedFields.add(suggestConfigs.get(i).toString());
            }
        }
        return suggestedFields;
    }

    private static List<String> loadResyncRestEndpoints() {
        List<String> resyncRestEndpoints = new ArrayList<>();
        JSONArray resyncEndPoints = (JSONArray) getJsonConfig().get("resyncRestEndpoints");
        if(resyncEndPoints!=null) {
            for(Object endPoint: resyncEndPoints) {
                resyncRestEndpoints.add(endPoint.toString());
            }
        }
        if(resyncRestEndpoints.isEmpty()) {
            throw new IllegalStateException("at least one resync rest end point needs to be configured");
        }
        return resyncRestEndpoints;
    }

    private static List<VisibilityConfiguration> loadNonViewableConfiguration() {
        return getVisibilityConfigurations((JSONArray) getJsonConfig().get("nonViewableMessages"));
    }

    private static List<VisibilityConfiguration> loadPartiallyViewableConfiguration() {
        return getVisibilityConfigurations((JSONArray) getJsonConfig().get("partiallyViewableMessages"));
    }

    private static List<String> loadEditableMessageTypes() {
        List<String> editableMessageTypes = new ArrayList<>();
        JSONArray entities = (JSONArray) getJsonConfig().get("editableMessageTypes");
        if(entities!=null) {
            for(Object entity: entities) {
                editableMessageTypes.add( entity.toString().toUpperCase() ); // this will make comparison more sane
            }
        }
        return editableMessageTypes;
    }

    private static List<String> loadResubmitBlackList() {
        List<String> resubmitBlackList = new ArrayList<>();
        JSONArray entities = (JSONArray) getJsonConfig().get("resubmitBlackList");
        if(entities!=null) {
            for(Object entity: entities) {
                resubmitBlackList.add( entity.toString().toUpperCase() ); // this will make comparison more sane
            }
        }
        return resubmitBlackList;
    }

    private static List<String> loadResubmitRestEndpoints() {
        List<String> resubmitRestEndpoints = new ArrayList<>();
        JSONArray endpoints = (JSONArray) getJsonConfig().get("resubmitRestEndpoints");
        if(endpoints!=null) {
            for(Object endpoint: endpoints) {
                resubmitRestEndpoints.add( endpoint.toString() ); // this will make comparison more sane
            }
        }
        return resubmitRestEndpoints;
    }
    private static List<VisibilityConfiguration> getVisibilityConfigurations(JSONArray jsonConfigurations) {

        List<VisibilityConfiguration> result = new ArrayList<>();
        for(int i=0; i<jsonConfigurations.size(); i++) {
            VisibilityConfiguration conf = new VisibilityConfiguration();
            JSONObject jsonConfiguration = (JSONObject) jsonConfigurations.get(i);
            conf.setMatchCriteriaMap(getMap((JSONObject) jsonConfiguration.get("matchCriteria")));
            conf.setConfigurationMap(getMap((JSONObject) jsonConfiguration.get("configuration")));
            result.add(conf);
        }
        return result;
    }

    private static Map<String, String> getMap(JSONObject jsonMap) {
        Map<String,String> map = new HashMap<>();
        for(Map.Entry<String,String> entry : (Set<Map.Entry<String,String>>)jsonMap.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        return map;
    }


}

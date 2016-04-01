package org.esbtools.message.admin.model;

/**
 * Models a configuration used in searches to be populated from a configuration file
 */
public class MessageSearchConfiguration {
    /**
     * The display value for the configuration
     */
    private String label;

    /**
     * The value that will be passed to the controller from the frontend
     */
    private String value;

    /**
     * A list of systems that this configuration is applicable for. This will be null if
     * none exist
     */
    private String[] availableEntities;

    public MessageSearchConfiguration() {
        this.label = "";
        this.value = "";
    }

    public MessageSearchConfiguration(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String[] getAvailableEntities() {
        return availableEntities;
    }

    public void setAvailableEntities(String[] availableEntities) {
        this.availableEntities = availableEntities;
    }

}

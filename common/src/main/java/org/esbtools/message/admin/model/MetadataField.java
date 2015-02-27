package org.esbtools.message.admin.model;

import java.util.LinkedList;
import java.util.List;

public class MetadataField {

    private Long id;
    private String name;
    private MetadataType type;
    private String value;
    private List<MetadataField> children;
    private List<MetadataField> suggestions;

    public MetadataField() {
        children = new LinkedList<MetadataField>();
        suggestions = new LinkedList<MetadataField>();
    }

    public MetadataField(MetadataType type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
        children = new LinkedList<MetadataField>();
        suggestions = new LinkedList<MetadataField>();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public MetadataType getType() {
        return type;
    }
    public void setType(MetadataType argType) {
        this.type = argType;
    }
    public String getName() {
        return name;
    }
    public void setName(String path) {
        this.name = path;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public List<MetadataField> getChildren() {
        return children;
    }
    public void setChildren(List<MetadataField> children) {
        this.children = children;
    }
    public List<MetadataField> getSuggestions() {
        return suggestions;
    }
    public void setSuggestions(List<MetadataField> suggestions) {
        this.suggestions = suggestions;
    }
    public void addDescendant(MetadataField descendant) {
        if (descendant.getType() == MetadataType.Suggestion) {
            getSuggestions().add(descendant);
        } else {
            getChildren().add(descendant);
        }
    }
    public void addDescendants(List<MetadataField> descendants) {
        for (MetadataField descendant : descendants)
            addDescendant(descendant);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format(" MetadataField (%d) %s: %s = %s", id, type.name(), name, value));
        for (MetadataField child : children)
            result.append(child.toString());
        for (MetadataField suggestion : suggestions)
            result.append(suggestion.toString());
        return result.toString();
    }
}

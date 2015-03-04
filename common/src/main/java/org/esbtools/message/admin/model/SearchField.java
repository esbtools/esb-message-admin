package org.esbtools.message.admin.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public enum SearchField {
    id(Long.class),
    errorQueue(String.class),
    messageId(Long.class),
    messageGuid(String.class),
    messageType(String.class),
    sourceQueue(String.class),
    sourceSystem(String.class),
    originalSystem(String.class),
    queueName(String.class),
    queueLocation(String.class),
    errorComponent(String.class),
    serviceName(String.class),
    customHeader(String.class);

    private Class valueType;

    // Reverse-lookup map
    private static final Set<String> lookup = new HashSet<String>();
    static {
        for (SearchField f : SearchField.values())
            lookup.add(f.name());
    }

    private SearchField(Class type) {
        this.valueType = type;
    }

    public Class getValueType() {
        return valueType;
    }

    public static boolean isPreDefined(String key) {
        if(lookup.contains(key))
            return true;
        return false;
    }

    public static List<String> find(String pattern) {
        List<String> match = new ArrayList<String>();
        for (SearchField field : SearchField.values()) {
            if (field.name().toLowerCase().contains(pattern.toLowerCase())) {
                match.add(field.name());
            }
        }
        return match;
    }
}

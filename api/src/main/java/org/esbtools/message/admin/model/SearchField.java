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
package org.esbtools.message.admin.model;

import java.util.HashSet;
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
    private static final Set<String> LOOKUP = new HashSet<String>();
    static {
        for (SearchField f : SearchField.values()) {
            LOOKUP.add(f.name().toUpperCase());
        }
    }

    private SearchField(Class type) {
        this.valueType = type;
    }

    public Class getValueType() {
        return valueType;
    }

    public static boolean isPreDefined(String key) {
        return LOOKUP.contains(key.toUpperCase()) ? true : false;
    }
    
    public static SearchField match(String pattern) {
        SearchField match = null;
        for (SearchField field : SearchField.values()) {
            if (field.name().equalsIgnoreCase(pattern)) {
                match = field; 
            }
        }
        return match;
    }
}

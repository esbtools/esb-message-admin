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

import java.io.Serializable;

/**
 * Base class for Single search criterion. Keeps the search field and value.
 *
 * This class accepts both String and Long values. The value is interpreted
 * based on the field during search criteria construction. That is, if the field
 * is string (e.g. user name), then the search construction algorithm calls
 * {@link #getStringValue() getStringValue()}, whereas if the field is a Long
 * (e.g. account number), the search construction algorithm calls
 * {@link #getLongValue() getLongValue()}. It is possible to pass a valid String
 * or Number insead of the Long value as long as the value can be converted to
 * Long.
 *
 */
public class Criterion implements Serializable {

    private static final long serialVersionUID = 1;

    private SearchField field;
    private boolean isCustom = false;
    private String customKey;
    private Object value;

    /**
     * Default Constructor, constructs an empty object
     */
    public Criterion() {
    }

    /**
     * Constructor for Object value
     */
    public Criterion(SearchField field, Object value) {
        if (field == null)
            throw new IllegalArgumentException("field is null");
        this.setField(field);
        this.value = value;
    }
    
    /**
     * Constructor for Object value
     */
    public Criterion(String customKey, Object value) {
        this.setCustomKey(customKey);
        this.value = value;
    }

    /**
     * Constructor for long value
     */
    public Criterion(Long value) {
        if(value==null)
            throw new IllegalArgumentException("value is null");
        this.value=value;
    }

    public Criterion(Object value) {
        this.value=value;
    }

    /**
     * Gets search value as a long. Returns null if the search value is not a number.
     *
     * @return the value of value
     */
    public Long getLongValue()  {
        if(value!=null) {
            if(value instanceof Long)
                return (Long)value;
            else if(value instanceof Number)
                return Long.valueOf(((Number)value).longValue());
            else if(value instanceof String)
                return Long.valueOf((String)value);
            else
                throw new NumberFormatException("value");
        }
        return null;
    }

    /**
     * Returns the value
     */
    public Object getObjectValue() {
        return value;
    }

    /**
     * Sets the value
     */
    public void setObjectValue(Object b) {
        value=b;
    }

    /**
     * Gets search value as a string.
     *
     * @return the value of value
     */
    public String getStringValue()  {
        return value==null?null:value.toString();
    }

    /**
     * Does the same as 'setValue(string)' but has a special name that the container
     * requires to correctly generate a WSDL that will allow you to set the value
     * @param argValue
     */
    public void setStringValue(String argValue) {
        this.value = argValue;
    }

    /**
     * Sets the value as a string
     *
     * @param argValue Value to assign to this.value
     */
    public void setValue(String argValue) {
        this.value = argValue;
    }

    /**
     * Sets the value to a number
     */
    public void setValue(Long argValue) {
        this.value = argValue;
    }

    /**
     * Gets the value of field
     *
     * @return the value of field
     */
    public SearchField getField() {
        return this.field;
    }

    /**
     * Sets the value of field
     *
     * @param argField
     *            Value to assign to this.field
     */
    public void setField(SearchField argField) {
        this.setCustom(false);
        this.field = argField;
    }

    /**
     * @return the isCustom
     */
    public boolean isCustom() {
        return isCustom;
    }

    /**
     * @param isCustom the isCustom to set
     */
    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    /**
     * @return the customKey
     */
    public String getCustomKey() {
        return customKey;
    }

    /**
     * @param customKey the customKey to set
     */
    public void setCustomKey(String customKey) {
        this.setCustom(true);
        this.customKey = customKey;
    }

    /*
     * @return String representation of key, whether its pre defined or custom
     * key
     */
    public String getKeyString() {
        if (isCustom())
            return getCustomKey();
        return getField().name();

    }

    @Override
    public String toString() {
        String keyValue = getKeyString() + "=" + value.toString() + ";";
        if (this.isCustom())
            return "Custom Field: " + keyValue;
        return "PreDefined Field: " + keyValue;
    }
}

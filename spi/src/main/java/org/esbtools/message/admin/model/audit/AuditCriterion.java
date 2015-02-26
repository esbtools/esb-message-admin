package org.esbtools.message.admin.model.audit;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * AuditCriterion objects are used to define search criteria on
 * audit events. Each criterion object specifies a search field and
 * a value to search on.
 *
 * @author ykoer
 */
@JsonIgnoreProperties(ignoreUnknown = true) //Allow fields to be added to model without breaking clients
public class AuditCriterion implements Serializable {

    public enum Field {
           LOGGED_DATE_FROM("loggedTime", Date.class),
           LOGGED_DATE_TO("loggedTime", Date.class),
           PRINCIPAL("principal", String.class),
           ACTION("action",String.class),
           MESSAGE_TYPE("messageType", String.class),
           MESSAGE_KEY("messageKey", String.class),
           KEY_TYPE("keyType", String.class);

           private String fieldName;
           private Class<?> valueType;

           private Field(String fieldName, Class<?> valueType) {
               this.fieldName = fieldName;
               this.valueType = valueType;
           }

           public String getFieldName() {
               return fieldName;
           }

           public Class<?> getValueType() {
               return valueType;
           }
    }

    private Field field;
    private Object value;

    /**
     * Creates a search criterion, and validates that the given value
     * can be used with the given search field.
     */
    public AuditCriterion(Field field, Object value) {
        if(field==null)
            throw new IllegalArgumentException("field is null");
        if(value==null)
            throw new IllegalArgumentException("value is null");
        this.field=field;
        this.value=value;
        if(!value.getClass().equals(field.getValueType())) {
            // Check if the value can be converted to the field data type
            if(field.getValueType().equals(Date.class)&&
               getDateValue()==null)
                throw new IllegalArgumentException("Date value required with "+field);
        }
    }

    /**
     * The search field
     */
    public Field getField() {
        return field;
    }

    /**
     * The search field
     */
    public void setField(Field field) {
        this.field=field;
    }


    /**
     * Returns the value as a string.
     */
    public String getStringValue() {
        return value==null?null:value.toString();
    }

    /**
     * Sets the value to the given string
     */
    public void setStringValue(String s) {
        value=s;
    }

    /**
     * Attempts to return the current value as a Date. If the current
     * value is a String, it tries to parse the string with format
     * yyyy/MM/dd. If the value cannot be converted to date, returns
     * null.
     */
    public Date getDateValue() {
        if(value!=null) {
            if(value instanceof Date)
                return (Date)value;
            else if(value instanceof String) {
                try {
                    SimpleDateFormat fmt=new SimpleDateFormat("yyyy/MM/dd");
                    return fmt.parse( (String)value);
                } catch (ParseException x) {
                }
            }
        }
        return null;
    }

    /**
     * Sets the value to the given date
     */
    public void setDateValue(Date d) {
        value=d;
    }

    public static String toString(AuditCriterion[] criteria) {
        if(criteria!=null) {
            StringBuffer buf=new StringBuffer(criteria.length*64);
            for(AuditCriterion c:criteria)
                buf.append(c.toString()).append('\n');
            return buf.toString();
        }
        return "";
    }
}

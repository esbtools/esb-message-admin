package org.esbtools.message.admin.model;

public class Header {

    private HeaderType type;
    private String name;
    private String value;

    public Header() {
    }
    
    public Header(HeaderType type, String name, String value) {
        super();
        this.type = type;
        this.name = name;
        this.value = value;
    }


    public HeaderType getType() {
        return type;
    }
    public void setType(HeaderType typeCode) {
        this.type = typeCode;
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

    @Override
    public String toString() {
        return String.format("%s: %s = %s", type, name, value);
    }

}

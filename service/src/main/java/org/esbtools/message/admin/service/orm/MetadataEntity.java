package org.esbtools.message.admin.service.orm;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.esbtools.message.admin.model.MetadataType;

@Entity
@Table(name = "METADATA")
public class MetadataEntity implements Serializable {

    private static final long serialVersionUID = 357984147079041238L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="type")
    @Enumerated(EnumType.STRING)
    private MetadataType type;

    @Column(name="name")
    private String name;

    @Column(name="value")
    private String value;

    @Column(name = "parent_id")
    private Long parentId;

    public MetadataEntity(MetadataType type, String name, String value, Long parentId) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.parentId = parentId;
    }

    public MetadataEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetadataType getType() {
        return type;
    }

    public void setType(MetadataType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long id) {
        this.parentId = id;
    }

    /*
     * Entities -> Entity -> System -> SyncKey
     * SearchKeys -> SearchKey -> [ *Path | Suggestion ]
     */
    public boolean canBeChildOf(MetadataType parentType) {

        if(this.type==MetadataType.Entity && parentType==MetadataType.Entities ||
            this.type==MetadataType.System && parentType==MetadataType.Entity ||
            this.type==MetadataType.SyncKey && parentType==MetadataType.System ||
            this.type==MetadataType.SearchKey && parentType==MetadataType.SearchKeys ||
            parentType==MetadataType.SearchKey && ( this.type==MetadataType.XPATH || this.type == MetadataType.Suggestion))
            return true;
        return false;
    }

    @Override
    public String toString() {
        return "MetadataEntity [id=" + id + ", type=" + type + ", name=" + name + ", value=" + value + ", parentId=" + parentId + "]";
    }

}

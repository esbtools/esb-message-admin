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
package org.esbtools.message.admin.common.orm;

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
        if(checkEntitiesChildren(parentType) || checkSearchKeysChildren(parentType)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkEntitiesChildren(MetadataType parentType) {
        return isEntityWithEntitiesParent(parentType) || isSystemWithEntityParent(parentType) || isSyncKeyWithSystemParent(parentType);
    }

    private boolean checkSearchKeysChildren(MetadataType parentType) {
        return isSearchKeyWithSearchKeysParent(parentType) || isXpathOrSuggestionWithSearchKeyParent(parentType);
    }

    private boolean isEntityWithEntitiesParent(MetadataType parentType) {
        return this.type==MetadataType.Entity && parentType==MetadataType.Entities;
    }

    private boolean isSystemWithEntityParent(MetadataType parentType) {
        return this.type==MetadataType.System && parentType==MetadataType.Entity;
    }

    private boolean isSyncKeyWithSystemParent(MetadataType parentType) {
        return this.type==MetadataType.SyncKey && parentType==MetadataType.System;
    }

    private boolean isSearchKeyWithSearchKeysParent(MetadataType parentType) {
        return this.type==MetadataType.SearchKey && parentType==MetadataType.SearchKeys;
    }

    private boolean isXpathOrSuggestionWithSearchKeyParent(MetadataType parentType) {
        return parentType==MetadataType.SearchKey && ( this.type==MetadataType.XPATH || this.type == MetadataType.Suggestion);
    }

    @Override
    public String toString() {
        return "MetadataEntity [id=" + id + ", type=" + type + ", name=" + name + ", value=" + value + ", parentId=" + parentId + "]";
    }

}

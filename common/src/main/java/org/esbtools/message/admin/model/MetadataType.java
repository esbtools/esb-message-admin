package org.esbtools.message.admin.model;

public enum MetadataType {
    Entities, Entity, System, SyncKey, SearchKeys, SearchKey, XPATH, Suggestion;

    public boolean isSearchKeyType() {
        if(this==Entities || this==Entity || this==System || this==SyncKey) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSyncKeyType() {
        return !isSearchKeyType();
    }
}

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

public class MetadataResponse {

    public static enum Status {
        Success, Error
    }

    private MetadataField tree;
    private MetadataField resultField;
    private String hash;
    private Status status = Status.Success;
    private String errorMessage;

    /**
     * @return the root
     */
    public MetadataField getTree() {
        return tree;
    }
    /**
     * @param root
     *            the root to set
     */
    public void setTree(MetadataField root) {
        this.tree = root;
    }
    /**
     * @return the resultField
     */
    public MetadataField getResult() {
        return resultField;
    }
    /**
     * @param resultField
     *            the resultField to set
     */
    public void setResult(MetadataField result) {
        this.resultField = result;
    }
    @Override
    public String toString() {
        return "MetadataResult [root=" + tree + ", resultField=" + resultField + "]";
    }
    /**
     * @return the hash
     */
    public String getHash() {
        return hash;
    }
    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.setStatus(Status.Error);
    }

}

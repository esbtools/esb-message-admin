package org.esbtools.message.admin.model;

public class MetadataResponse {

    public static enum Status {
        Success, Error
    };
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

package com.old.silence.job.client.retry.core.context;



import java.math.BigInteger;



public final class RemoteRetryContext {

    private String namespaceId;
    private String groupName;
    private String scene;
    private String argsStr;
    private String executorName;
    private Integer retryCount;
    private BigInteger retryTaskId;
    private BigInteger retryId;
    private Object[] deSerialize;

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public BigInteger getRetryTaskId() {
        return retryTaskId;
    }

    public void setRetryTaskId(BigInteger retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }

    public Object[] getDeSerialize() {
        return deSerialize;
    }

    public void setDeSerialize(Object[] deSerialize) {
        this.deSerialize = deSerialize;
    }
}

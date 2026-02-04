package com.old.silence.job.client.retry.core.context;

import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.common.enums.RetryStatus;

import java.math.BigInteger;


public final class CallbackContext {

    private String namespaceId;
    private String groupName;
    private String sceneName;
    private BigInteger retryTaskId;
    private BigInteger retryId;
    private RetryStatus retryStatus;
    private Object[] deSerialize;
    private RetryerInfo retryerInfo;

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

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
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

    public RetryStatus getRetryStatus() {
        return retryStatus;
    }

    public void setRetryStatus(RetryStatus retryStatus) {
        this.retryStatus = retryStatus;
    }

    public Object[] getDeSerialize() {
        return deSerialize;
    }

    public void setDeSerialize(Object[] deSerialize) {
        this.deSerialize = deSerialize;
    }

    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

    public void setRetryerInfo(RetryerInfo retryerInfo) {
        this.retryerInfo = retryerInfo;
    }
}

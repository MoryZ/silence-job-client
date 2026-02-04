package com.old.silence.job.client.retry.core.retryer;


public enum RetryType {

    /**
     * 本地重试: 发生异常时通过内存进行重试
     */
    ONLY_LOCAL,

    /**
     * 远程重试: 发生异常时将数据上报到服务端进行重试
     */
    ONLY_REMOTE,

    /**
     * 先本地重试，在远程重试
     * 即：现在本地进行内存重试N次，如果本地重试未解决，将异常数据上报到服务端进行重试
     */
    LOCAL_REMOTE
}

package com.old.silence.job.client.common.rpc.supports.scan;

import com.old.silence.job.client.common.rpc.client.RequestMethod;

import java.lang.reflect.Method;


public class EndPointInfo {

    private final String executorName;
    private final Method method;
    private final Object executor;
    private final RequestMethod requestMethod;
    private final String path;

    public EndPointInfo(String executorName, Method method, Object executor, RequestMethod requestMethod, String path) {
        this.executorName = executorName;
        this.method = method;
        this.executor = executor;
        this.requestMethod = requestMethod;
        this.path = path;
    }

    public String getExecutorName() {
        return executorName;
    }

    public Method getMethod() {
        return method;
    }

    public Object getExecutor() {
        return executor;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getPath() {
        return path;
    }
}

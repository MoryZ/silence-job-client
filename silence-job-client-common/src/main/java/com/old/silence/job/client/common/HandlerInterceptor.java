package com.old.silence.job.client.common;


import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;
import com.old.silence.job.client.common.rpc.supports.scan.EndPointInfo;

public interface HandlerInterceptor {

    boolean preHandle(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler);

    void postHandle(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler);

    void afterCompletion(HttpRequest httpRequest, HttpResponse httpResponse, EndPointInfo handler, Exception ex);

    int order();
}

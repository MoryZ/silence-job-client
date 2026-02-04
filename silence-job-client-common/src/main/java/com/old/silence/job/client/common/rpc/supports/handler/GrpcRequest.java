package com.old.silence.job.client.common.rpc.supports.handler;


import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;



/**
 * netty客户端请求模型
 *
 */
public class GrpcRequest {

    private GrpcSilenceJobRequest silenceJobRequest;
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;

    public GrpcRequest(HttpResponse httpResponse, HttpRequest httpRequest, GrpcSilenceJobRequest silenceJobRequest) {
        this.httpResponse = httpResponse;
        this.httpRequest = httpRequest;
        this.silenceJobRequest = silenceJobRequest;
    }

    public GrpcSilenceJobRequest getSilenceJobRequest() {
        return silenceJobRequest;
    }

    public void setSilenceJobRequest(GrpcSilenceJobRequest silenceJobRequest) {
        this.silenceJobRequest = silenceJobRequest;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}

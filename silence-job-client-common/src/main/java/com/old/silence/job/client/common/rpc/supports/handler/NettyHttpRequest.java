package com.old.silence.job.client.common.rpc.supports.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;



/**
 * netty客户端请求模型
 *
 */
public class NettyHttpRequest {

    private final ChannelHandlerContext channelHandlerContext;
    private final String content;
    private final boolean keepAlive;
    private final HttpMethod method;
    private final String uri;
    private final HttpHeaders headers;
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;

    public NettyHttpRequest(ChannelHandlerContext channelHandlerContext, String content, boolean keepAlive, HttpMethod method, String uri, HttpHeaders headers, HttpResponse httpResponse, HttpRequest httpRequest) {
        this.channelHandlerContext = channelHandlerContext;
        this.content = content;
        this.keepAlive = keepAlive;
        this.method = method;
        this.uri = uri;
        this.headers = headers;
        this.httpResponse = httpResponse;
        this.httpRequest = httpRequest;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public String getContent() {
        return content;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }
}

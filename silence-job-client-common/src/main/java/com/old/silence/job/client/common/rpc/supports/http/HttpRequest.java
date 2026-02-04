package com.old.silence.job.client.common.rpc.supports.http;



import java.util.Map;


public class HttpRequest {
    private final Map<String, String> headers;
    private final String uri;

    public HttpRequest(Map<String, String> headers, String uri) {
        this.headers = headers;
        this.uri = uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getUri() {
        return uri;
    }
}

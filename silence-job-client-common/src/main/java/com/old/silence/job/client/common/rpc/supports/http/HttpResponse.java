package com.old.silence.job.client.common.rpc.supports.http;


import java.util.HashMap;
import java.util.Map;


public class HttpResponse {

    private Map<String, Object> headers = new HashMap<>();

    public HttpResponse() {
    }

    public void setHeader(String key, Object value) {
        headers.put(key, value);
    }

    public HttpResponse(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}

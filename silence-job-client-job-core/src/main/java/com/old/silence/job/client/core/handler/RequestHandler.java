package com.old.silence.job.client.core.handler;

import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.client.core.openapi.OpenApiClient;
import com.old.silence.job.common.model.SilenceJobRpcResult;

public interface RequestHandler<R> {

    OpenApiClient client = RequestBuilder.<OpenApiClient, SilenceJobRpcResult>newBuilder()
            .client(OpenApiClient.class)
            .async(false)
            .build();

    R execute();

}
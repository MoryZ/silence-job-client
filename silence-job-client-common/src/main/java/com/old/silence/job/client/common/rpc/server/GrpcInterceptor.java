package com.old.silence.job.client.common.rpc.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcInterceptor implements ServerInterceptor {


    private static final Logger log = LoggerFactory.getLogger(GrpcInterceptor.class);

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
                                                      ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        long start = System.currentTimeMillis();

        try {
            return serverCallHandler.startCall(serverCall, metadata);
        } finally {
            log.info("method invoked: {} cast:{}ms", fullMethodName, System.currentTimeMillis() - start);
        }
    }

}

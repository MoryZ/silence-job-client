package com.old.silence.job.client.common.rpc.supports.handler;

import cn.hutool.core.util.StrUtil;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.common.grpc.auto.Metadata;
import com.old.silence.job.common.model.SilenceJobRpcResult;

import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class UnaryRequestHandler implements ServerCalls.UnaryMethod<GrpcSilenceJobRequest, GrpcResult> {


    private final ThreadPoolExecutor dispatcherThreadPool;
    private final SilenceDispatcherRequestHandler dispatcher;

    public UnaryRequestHandler(SilenceJobProperties.ThreadPoolConfig dispatcherThreadPool,
                               final SilenceDispatcherRequestHandler handler) {
        this.dispatcher = handler;
        this.dispatcherThreadPool = new ThreadPoolExecutor(
            dispatcherThreadPool.getCorePoolSize(), dispatcherThreadPool.getMaximumPoolSize(),
            dispatcherThreadPool.getKeepAliveTime(),
            dispatcherThreadPool.getTimeUnit(), new LinkedBlockingQueue<>(dispatcherThreadPool.getQueueCapacity()),
            new CustomizableThreadFactory("silence-grpc-server-"));
    }

    @Override
    public void invoke(GrpcSilenceJobRequest grpcSilenceJobRequest, StreamObserver<GrpcResult> streamObserver) {

        Metadata metadata = grpcSilenceJobRequest.getMetadata();

        GrpcRequest grpcRequest = new GrpcRequest(new HttpResponse(), new HttpRequest(metadata.getHeadersMap(), metadata.getUri()), grpcSilenceJobRequest);

        // 执行任务
        dispatcherThreadPool.execute(() -> {
            SilenceJobRpcResult silenceJobRpcResult = null;
            try {
                silenceJobRpcResult = dispatcher.dispatch(grpcRequest);
            } catch (Exception e) {
                silenceJobRpcResult = new SilenceJobRpcResult(500, e.getMessage(), null, 0);
            } finally {
                GrpcResult grpcResult = GrpcResult.newBuilder()
                    .setStatus(silenceJobRpcResult.getCode())
                    .setMessage(Optional.ofNullable(silenceJobRpcResult.getMessage()).orElse(StrUtil.EMPTY))
                    .setData(JSON.toJSONString(silenceJobRpcResult.getData()))
                    .build();

                streamObserver.onNext(grpcResult);
                streamObserver.onCompleted();
            }
        });

    }
}

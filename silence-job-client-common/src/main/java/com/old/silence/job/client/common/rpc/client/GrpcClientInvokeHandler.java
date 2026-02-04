package com.old.silence.job.client.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson2.JSON;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.exception.SilenceJobClientTimeOutException;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 请求处理器
 *
 */
public class GrpcClientInvokeHandler<R extends ApiResult<Object>> implements InvocationHandler {

    public static final AtomicLong REQUEST_ID = new AtomicLong(0);
    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;

    private static final ExecutorService executorService = createGrpcExecutor();

    private static final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1);

    public GrpcClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);

        long reqId = newId();
        ListenableFuture<GrpcResult> future = GrpcChannel.sendOfUnary(annotation.path(), JSON.toJSONString(args),
            reqId);
        SilenceJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", sw.getTotalTimeMillis(), reqId);
        if (future == null) {
            return (R) new SilenceJobRpcResult(500, "future is nulll", null, reqId);
        }

        if (async) {
            Futures.addCallback(future, new FutureCallback<>() {

                @Override
                public void onSuccess(GrpcResult result) {

                    Object obj = JSON.parseObject( result.getData(), Object.class);
                    consumer.accept(
                            (R) new SilenceJobRpcResult(result.getStatus(), result.getMessage(), obj, result.getReqId()));
                }

                @Override
                public void onFailure(Throwable t) {
                    consumer.accept((R) new SilenceJobRpcResult(500, t.getMessage(), null, reqId));
                }
            }, executorService);

            Futures.withTimeout(future, timeout, unit, schedule);
            return null;
        } else {

            try {
                GrpcResult result = future.get(timeout, unit);
                Object obj = JSON.parseObject(result.getData(), Object.class);
                return (R) new SilenceJobRpcResult(result.getStatus(), result.getMessage(), obj, result.getReqId());
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                throw new SilenceJobClientTimeOutException("Request to remote interface timed out. path:[{}]",
                    annotation.path());
            }
        }

    }

    private static long newId() {
        return REQUEST_ID.getAndIncrement();
    }

    protected static ThreadPoolExecutor createGrpcExecutor() {
        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);
        SilenceJobProperties.RpcClientProperties clientRpc = silenceJobProperties.getClientRpc();
        String serverIp = GrpcChannel.getServerHost().replaceAll("%", "-");
        SilenceJobProperties.ThreadPoolConfig threadPool = clientRpc.getClientTp();
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("silence-job-grpc-client-executor-" + serverIp + "-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }
}

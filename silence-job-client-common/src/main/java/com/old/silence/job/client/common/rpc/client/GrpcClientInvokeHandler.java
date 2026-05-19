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

        // 调试日志：打印实际发送的参数
        SilenceJobLog.LOCAL.info("=== GrpcClient Debug: method=[{}], args=[{}], args.length=[{}]", 
            method.getName(), JSON.toJSONString(args), args != null ? args.length : "null");
        if (args != null && args.length > 0) {
            SilenceJobLog.LOCAL.info("=== GrpcClient Debug: args[0] class=[{}], isArray=[{}]", 
                args[0].getClass().getName(), args[0].getClass().isArray());
            // 如果是数组，递归检查
            if (args[0].getClass().isArray()) {
                Object[] innerArray = (Object[]) args[0];
                SilenceJobLog.LOCAL.info("=== GrpcClient Debug: args[0] length=[{}]", innerArray.length);
                if (innerArray.length > 0) {
                    SilenceJobLog.LOCAL.info("=== GrpcClient Debug: args[0][0] class=[{}]", innerArray[0].getClass().getName());
                }
            }
            SilenceJobLog.LOCAL.info("=== GrpcClient Debug: args[0] toString=[{}]", args[0].toString());
        }

        long reqId = newId();
        // 处理 args 为 null 或空数组的情况
        String body;
        if (args == null || args.length == 0) {
            body = "[]";
        } else {
            // 检查是否所有元素都是 null，如果是则发送空数组
            boolean allNull = true;
            for (Object arg : args) {
                if (arg != null) {
                    allNull = false;
                    break;
                }
            }
            body = allNull ? "[]" : JSON.toJSONString(args);
        }
        SilenceJobLog.LOCAL.info("=== GrpcClient Debug: body=[{}]", body);
        ListenableFuture<GrpcResult> future = GrpcChannel.sendOfUnary(annotation.path(), body, reqId);
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

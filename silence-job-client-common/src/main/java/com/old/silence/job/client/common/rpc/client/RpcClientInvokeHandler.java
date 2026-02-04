package com.old.silence.job.client.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import io.netty.handler.codec.http.HttpMethod;
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.common.exception.SilenceJobClientTimeOutException;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.rpc.RpcContext;
import com.old.silence.job.common.rpc.SilenceJobFuture;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 请求处理器
 *
 */
public class RpcClientInvokeHandler<R extends ApiResult<Object>> implements InvocationHandler {

    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;

    public RpcClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);
        SilenceJobRequest silenceJobRequest = new SilenceJobRequest(args);

        sw.start("request start " + silenceJobRequest.getReqId());

        SilenceJobFuture<R> newFuture = SilenceJobFuture.newFuture(silenceJobRequest.getReqId(),
                timeout,
                unit);
        RpcContext.setFuture(newFuture);

        try {
            NettyChannel.send(HttpMethod.valueOf(annotation.method().name()), annotation.path(), silenceJobRequest.toString());
        } finally {
            sw.stop();
        }

        SilenceJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", silenceJobRequest.getReqId(), sw.getTotalTimeMillis());
        if (async) {
            newFuture.whenComplete((r, t) -> {
                if (Objects.nonNull(t)) {
                    consumer.accept(
                            (R) new SilenceJobRpcResult(500, t.getMessage(), null, silenceJobRequest.getReqId()));
                } else {
                    consumer.accept(r);
                }
            });
            return null;
        } else {
            Assert.notNull(newFuture, () -> new SilenceJobClientException("completableFuture is null"));
            try {
                return newFuture.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                throw new SilenceJobClientTimeOutException("Request to remote interface timed out. path:[{}]", annotation.path());
            }
        }

    }

}

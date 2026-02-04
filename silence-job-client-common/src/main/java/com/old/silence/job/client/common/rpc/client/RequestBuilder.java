package com.old.silence.job.client.common.rpc.client;

import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.common.model.ApiResult;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 构建请求类型
 *
 */
public class RequestBuilder<T, R extends ApiResult<Object>> {

    private Class<T> clintInterface;

    private Consumer<R> callback;

    private boolean async = true;

    private long timeout = 60 * 1000;

    private TimeUnit unit = TimeUnit.MILLISECONDS;

    public static <T, R extends ApiResult<Object>> RequestBuilder<T, R> newBuilder() {
        return new RequestBuilder<>();
    }

    public RequestBuilder<T, R> client(Class<T> clintInterface) {
        this.clintInterface = clintInterface;
        return this;
    }

    public RequestBuilder<T, R> callback(Consumer<R> callback) {
        this.callback = callback;
        return this;
    }

    public RequestBuilder<T, R> async(boolean async) {
        this.async = async;
        return this;
    }

    public RequestBuilder<T, R> timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RequestBuilder<T, R> unit(TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public T build() {
        if (Objects.isNull(clintInterface)) {
            throw new SilenceJobClientException("clintInterface cannot be null");
        }

        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new SilenceJobClientException("class not found exception to: [{}]", clintInterface.getName());
        }

        InvocationHandler invocationHandler;
        SilenceJobProperties properties = SilenceSpringContext.getBean(SilenceJobProperties.class);
        RpcType rpcType = properties.getRpcType();
        if (Objects.isNull(rpcType)) {
            invocationHandler= new RpcClientInvokeHandler<>(async, timeout, unit,
                callback);
        } else {
            invocationHandler = new GrpcClientInvokeHandler<>(async, timeout, unit, callback);
        }

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
            new Class[]{clintInterface}, invocationHandler);
    }

}

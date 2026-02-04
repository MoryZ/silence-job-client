package com.old.silence.job.client.retry.core.loader;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.client.retry.core.RetryArgSerializer;
import com.old.silence.job.client.retry.core.RetrySiteSnapshotContext;
import com.old.silence.job.client.retry.core.event.SilenceJobListener;
import com.old.silence.job.client.retry.core.event.SimpleSilenceRetryListener;
import com.old.silence.job.client.retry.core.expression.ExpressionInvocationHandler;
import com.old.silence.job.client.retry.core.intercepter.ThreadLockRetrySiteSnapshotContext;
import com.old.silence.job.client.retry.core.serializer.JacksonSerializer;
import com.old.silence.job.common.expression.ExpressionEngine;
import com.old.silence.job.common.expression.ExpressionFactory;
import com.old.silence.job.common.expression.strategy.SpELExpressionEngine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * silence job Spi加载器
 *
 */
public class SilenceRetrySpiLoader {

    private SilenceRetrySpiLoader() {
    }

    /**
     * 加载参数序列化SPI类
     * 若配置多个则只加载第一个
     *
     * @return {@link com.old.silence.job.client.retry.core.serializer.JacksonSerializer} 默认序列化类为JacksonSerializer
     */
    public static RetryArgSerializer loadRetryArgSerializer() {
        return Optional.ofNullable(ServiceLoaderUtil.loadFirst(RetryArgSerializer.class)).orElse(new JacksonSerializer());
    }

    /**
     * 加载重试监听器SPI类
     * 执行顺序按照文件中定义的实现类的先后顺序
     *
     * @return {@link com.old.silence.job.client.retry.core.event.SimpleSilenceRetryListener} 默认序列化类为SimpleSilenceJobListener
     */
    public static List<SilenceJobListener> loadSilenceJobListener() {
        List<SilenceJobListener> silenceJobListeners = ServiceLoaderUtil.loadList(SilenceJobListener.class);
        if (CollectionUtils.isEmpty(silenceJobListeners)) {
            return Collections.singletonList(new SimpleSilenceRetryListener());
        }

        return silenceJobListeners;
    }

    /**
     * 加载重试现场记录上下文SPI类
     *
     * @return {@link RetrySiteSnapshotContext} 默认序列化类为ThreadLockRetrySiteSnapshotContext
     */
    public static <T> RetrySiteSnapshotContext<T> loadRetrySiteSnapshotContext() {
        return Optional.ofNullable(ServiceLoaderUtil.loadFirst(RetrySiteSnapshotContext.class)).orElse(new ThreadLockRetrySiteSnapshotContext<T>(new ThreadLocal<>()));
    }

    /**
     * 表达式引擎SPI类
     *
     * @return {@link SpELExpressionEngine} 默认序列化类为SpELExpressionEngine
     */
    public static ExpressionEngine loadExpressionEngine() {
        ExpressionEngine expressionEngine = Optional.ofNullable(ServiceLoaderUtil.loadFirst(ExpressionEngine.class)).orElse(new SpELExpressionEngine());
        return ExpressionFactory.getExpressionEngine(new ExpressionInvocationHandler(expressionEngine));
    }

}

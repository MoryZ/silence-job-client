package com.old.silence.job.client.retry.core.register.scan;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.retry.core.IdempotentIdGenerate;
import com.old.silence.job.client.retry.core.Scanner;
import com.old.silence.job.client.retry.core.annotation.ExecutorMethodRegister;
import com.old.silence.job.client.retry.core.callback.complete.RetryCompleteCallback;
import com.old.silence.job.client.retry.core.retryer.RetryType;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.strategy.ExecutorMethod;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 扫描手动注入重试方法
 *
 */
@Component

public class ExecutorMethodScanner implements Scanner, ApplicationContextAware {

    public ApplicationContext applicationContext;

    @Override
    public List<RetryerInfo> doScan() {
        List<RetryerInfo> retryerInfoList = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);

        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            ExecutorMethodRegister annotation = bean.getClass().getAnnotation(ExecutorMethodRegister.class);
            if (Objects.nonNull(annotation)) {
                RetryerInfo retryerInfo = resolvingRetryable(annotation, bean);
                Optional.ofNullable(retryerInfo).ifPresent(retryerInfoList::add);
            }
        }

        return retryerInfoList;
    }

    private RetryerInfo resolvingRetryable(ExecutorMethodRegister retryable, Object executor) {

        try {
            Class executorNotProxy = AopUtils.getTargetClass(executor);
            String executorClassName = executorNotProxy.getName();
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryable.idempotentId();
            Method executorMethodName = executorNotProxy.getMethod("doExecute", Object.class);
            Class<? extends RetryCompleteCallback> retryCompleteCallback = retryable.retryCompleteCallback();
            boolean async = retryable.async();
            long timeout = retryable.timeout();
            TimeUnit unit = retryable.unit();
            boolean forceReport = retryable.forceReport();

            return new RetryerInfo(retryable.scene(),
                    executorClassName,
                    new HashSet<>(Collections.emptyList()),
                    new HashSet<>(Collections.emptyList()),
                    executor,
                    executorMethodName,
                    RetryType.ONLY_REMOTE,
                    1,
                    1,
                    idempotentIdGenerate,
                    retryable.bizNo(),
                    (Class<? extends ExecutorMethod>) executor.getClass(),
                    Boolean.TRUE,
                    retryCompleteCallback,
                    async,
                    forceReport,
                    timeout,
                    unit
            );
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("{}重试信息加载报错：{}", executor.getClass().getName(), e);
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

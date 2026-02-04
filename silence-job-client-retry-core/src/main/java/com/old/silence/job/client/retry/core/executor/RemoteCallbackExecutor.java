package com.old.silence.job.client.retry.core.executor;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;

import com.old.silence.job.client.retry.core.callback.complete.RetryCompleteCallback;
import com.old.silence.job.client.retry.core.context.CallbackContext;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.log.RetryLogMeta;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.enums.LogTypeEnum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 */
@Component

public class RemoteCallbackExecutor {

    /**
     * 执行服务端回调任务
     *
     * @param context silenceJobLogManager
     * @throws NoSuchMethodException NoSuchBeanDefinitionException
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public void doRetryCallback(CallbackContext context) throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        try {

            initLogContext(context);

            // 以Spring Bean模式回调
            doCallbackForSpringBean(context);

            // 上报执行成功
            SilenceJobLog.REMOTE.info("任务执行成功 taskId:[{}]", context.getRetryTaskId());
        } catch (NoSuchBeanDefinitionException e) {
            // 若不是SpringBean 则直接反射以普通类调用
            doCallbackForOrdinaryClass(context);
        } finally {
            SilenceJobLogManager.removeAll();
        }
    }

    private static void initLogContext(CallbackContext context) {
        // 初始化实时日志上下文
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        retryLogMeta.setRetryId(context.getRetryId());
        SilenceJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }

    /**
     * 以Spring Bean模式回调
     *
     */
    private void doCallbackForSpringBean(CallbackContext context) {
        RetryerInfo retryerInfo = context.getRetryerInfo();
        Object[] deSerialize = context.getDeSerialize();
        Class<? extends RetryCompleteCallback> retryCompleteCallbackClazz = retryerInfo.getRetryCompleteCallback();

        RetryCompleteCallback retryCompleteCallback = SilenceSpringContext.getBeanByType(retryCompleteCallbackClazz);
        switch (Objects.requireNonNull(context.getRetryStatus())) {
            case FINISH:
                retryCompleteCallback.doSuccessCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                        deSerialize);
                break;
            case MAX_COUNT:
                retryCompleteCallback.doMaxRetryCallback(retryerInfo.getScene(), retryerInfo.getExecutorClassName(),
                        deSerialize);
                break;
            default:
                throw new SilenceRetryClientException("回调状态异常");
        }

    }

    /**
     * 以普通类进行回调
     *
     * @return Result
     */
    private void doCallbackForOrdinaryClass(CallbackContext context) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        RetryerInfo retryerInfo = context.getRetryerInfo();
        Object[] deSerialize = context.getDeSerialize();
        Class<? extends RetryCompleteCallback> retryCompleteCallbackClazz = retryerInfo.getRetryCompleteCallback();

        RetryCompleteCallback retryCompleteCallback =retryCompleteCallbackClazz.getDeclaredConstructor().newInstance();
        Method method;
        switch (Objects.requireNonNull(context.getRetryStatus())) {
            case FINISH:
                method = retryCompleteCallbackClazz.getMethod("doSuccessCallback", String.class, String.class,
                        Object[].class);
                break;
            case MAX_COUNT:
                method = retryCompleteCallbackClazz.getMethod("doMaxRetryCallback", String.class, String.class,
                        Object[].class);
                break;
            default:
                throw new SilenceRetryClientException("回调状态异常");
        }

        Assert.notNull(method, () -> new SilenceRetryClientException("no such method"));
        ReflectionUtils.invokeMethod(method, retryCompleteCallback, retryerInfo.getScene(),
                retryerInfo.getExecutorClassName(), deSerialize);

        SilenceJobLog.REMOTE.info("任务执行成功 taskId:[{}] [{}]", context.getRetryTaskId());

    }

}

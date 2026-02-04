package com.old.silence.job.client.retry.core.intercepter;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterAdvice;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import com.google.common.base.Defaults;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.retry.core.annotation.Propagation;
import com.old.silence.job.client.retry.core.annotation.Retryable;
import com.old.silence.job.client.retry.core.cache.RetryerInfoCache;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;
import com.old.silence.job.client.retry.core.strategy.RetryStrategy;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.enums.RetryResultStatus;
import com.old.silence.job.common.model.SilenceJobHeaders;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.common.util.NetUtil;
import com.old.silence.job.log.SilenceJobLog;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.old.silence.job.common.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;



public class SilenceRetryInterceptor implements MethodInterceptor, AfterAdvice, Serializable, Ordered {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String retryErrorMoreThresholdTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    private final RetryStrategy retryStrategy;
    private final int order;

    public SilenceRetryInterceptor(int order, RetryStrategy localRetryStrategies) {
        this.order = order;
        this.retryStrategy = localRetryStrategies;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        String traceId = UUID.randomUUID().toString();

        SilenceJobLog.LOCAL.debug("Start entering the around method traceId:[{}]", traceId);
        Retryable retryable = getAnnotationParameter(invocation.getMethod());
        String executorClassName = invocation.getThis().getClass().getName();
        String methodEntrance = getMethodEntrance(retryable, executorClassName);

        if (Propagation.REQUIRES_NEW.equals(retryable.propagation())) {
            RetrySiteSnapshot.setMethodEntrance(methodEntrance);
        } else if (!RetrySiteSnapshot.existedMethodEntrance()) {
            RetrySiteSnapshot.setMethodEntrance(methodEntrance);
        } else {
            SilenceJobLog.LOCAL.debug("No need to set entrance signs:[{}]", traceId);
        }

        Throwable throwable = null;
        Object result = null;
        RetryerResultContext retryerResultContext;
        try {
            result = invocation.proceed();
        } catch (Throwable t) {
            throwable = t;
        } finally {

            SilenceJobLog.LOCAL.debug("Start retrying. traceId:[{}] scene:[{}] executorClassName:[{}]", traceId,
                    retryable.scene(), executorClassName);
            // 入口则开始处理重试
            retryerResultContext = doHandlerRetry(invocation, traceId, retryable, executorClassName, methodEntrance,
                    throwable);
        }

        SilenceJobLog.LOCAL.debug("Method return value is [{}]. traceId:[{}]", result, traceId, throwable);

        // 若是重试完成了, 则判断是否返回重试完成后的数据
        if (Objects.nonNull(retryerResultContext)) {
            // 重试成功直接返回结果 若注解配置了isThrowException=false 则不抛出异常
            if (retryerResultContext.getRetryResultStatus()
                    .equals(RetryResultStatus.SUCCESS)
                    || !retryable.isThrowException()) {

                // 若返回值是NULL且是基本类型则返回默认值
                Method method = invocation.getMethod();
                if (Objects.isNull(retryerResultContext.getResult()) && method.getReturnType().isPrimitive()) {
                    return Defaults.defaultValue(method.getReturnType());
                }

                return retryerResultContext.getResult();
            }
        }

        // 无需开启重试的场景，需要清除缓存信息
        if ((RetrySiteSnapshot.isMethodEntrance(methodEntrance) && !RetrySiteSnapshot.isRunning())) {
            RetrySiteSnapshot.removeAll();
        }

        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }

    }


    private RetryerResultContext doHandlerRetry(MethodInvocation invocation, String traceId, Retryable retryable,
                                                String executorClassName, String methodEntrance, Throwable throwable) {

        if (!RetrySiteSnapshot.isMethodEntrance(methodEntrance)
                || RetrySiteSnapshot.isRunning()
                || Objects.isNull(throwable)
                // 重试流量不开启重试
                || RetrySiteSnapshot.isRetryFlow()
                // 下游响应不重试码，不开启重试
                || RetrySiteSnapshot.isRetryForStatusCode()
                // 匹配异常信息
                || !validate(throwable, RetryerInfoCache.get(retryable.scene(), executorClassName))
        ) {
            if (!RetrySiteSnapshot.isMethodEntrance(methodEntrance)) {
                SilenceJobLog.LOCAL.debug("Non-method entry does not enable local retries. traceId:[{}] [{}]", traceId,
                        RetrySiteSnapshot.getMethodEntrance());
            } else if (RetrySiteSnapshot.isRunning()) {
                SilenceJobLog.LOCAL.debug("Existing running retry tasks do not enable local retries. traceId:[{}] [{}]",
                        traceId, RetrySiteSnapshot.EnumStage.valueOfStage(RetrySiteSnapshot.getStage()));
            } else if (Objects.isNull(throwable)) {
                SilenceJobLog.LOCAL.debug("No exception, no local retries. traceId:[{}]", traceId);
            } else if (RetrySiteSnapshot.isRetryFlow()) {
                SilenceJobLog.LOCAL.debug("Retry traffic does not enable local retries. traceId:[{}] [{}]", traceId,
                        RetrySiteSnapshot.getRetryHeader());
            } else if (RetrySiteSnapshot.isRetryForStatusCode()) {
                SilenceJobLog.LOCAL.debug("Existing exception retry codes do not enable local retries. traceId:[{}]",
                        traceId);
            } else if (!validate(throwable, RetryerInfoCache.get(retryable.scene(), executorClassName))) {
                SilenceJobLog.LOCAL.debug("Exception mismatch. traceId:[{}]", traceId);
            } else {
                SilenceJobLog.LOCAL.debug("Unknown situations do not enable local retry scenarios. traceId:[{}]",
                        traceId);
            }
            return null;
        }

        return openRetry(invocation, traceId, retryable, executorClassName, throwable);
    }

    private RetryerResultContext openRetry(MethodInvocation point, String traceId, Retryable retryable,
                                           String executorClassName, Throwable throwable) {

        try {

            // 标识重试流量
            initHeaders(retryable);

            RetryerResultContext context = retryStrategy.openRetry(retryable.scene(), executorClassName,
                    point.getArguments());
            if (RetryResultStatus.SUCCESS.equals(context.getRetryResultStatus())) {
                SilenceJobLog.LOCAL.debug("local retry successful. traceId:[{}] result:[{}]", traceId,
                        context.getResult());
            } else {
                SilenceJobLog.LOCAL.debug("local retry result. traceId:[{}] throwable:[{}]", traceId,
                        context.getThrowable());
            }

            return context;
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("retry component handling exception，traceId:[{}]", traceId, e);

            // 预警
            sendMessage(e);

        } finally {
            RetrySiteSnapshot.removeAll();
        }

        return null;
    }

    private void initHeaders(Retryable retryable) {

        SilenceJobHeaders silenceJobHeaders = new SilenceJobHeaders();
        silenceJobHeaders.setRetry(Boolean.TRUE);
        silenceJobHeaders.setRetryId(IdUtil.getSnowflakeNextIdStr());
        silenceJobHeaders.setDdl(GroupVersionCache.getDdl(retryable.scene()));
        RetrySiteSnapshot.setRetryHeader(silenceJobHeaders);
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(
                    RetryNotifyScene.CLIENT_COMPONENT_ERROR);
            if (Objects.nonNull(notify)) {
                SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);
                if (Objects.isNull(silenceJobProperties)) {
                    return;
                }
                List<ConfigDTO.Notify.Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(new ArrayList<>());
                for (ConfigDTO.Notify.Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(retryErrorMoreThresholdTextMessageFormatter,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    silenceJobProperties.getNamespace(),
                                    silenceJobProperties.getGroup(),
                                    Instant.now().atZone(ZoneId.systemDefault()).format(formatter),
                                    e.getMessage())
                            .title("retry component handling exception:[{}]", silenceJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());

                    Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipient.getNotifyType()))
                            .ifPresent(alarm -> alarm.asyncSendMessage(context));
                }

            }
        } catch (Exception e1) {
            SilenceJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }

    public String getMethodEntrance(Retryable retryable, String executorClassName) {

        if (Objects.isNull(retryable)) {
            return StrUtil.EMPTY;
        }

        return retryable.scene().concat("_").concat(executorClassName);
    }

    private Retryable getAnnotationParameter(Method method) {

        Retryable retryable = null;
        if (method.isAnnotationPresent(Retryable.class)) {
            //获取当前类的方法上标注的注解对象
            retryable = method.getAnnotation(Retryable.class);
        }

        if (retryable == null) {
            // 返回当前类或父类或接口方法上标注的注解对象
            retryable = AnnotatedElementUtils.findMergedAnnotation(method, Retryable.class);
        }

        return retryable;
    }

    @Override
    public int getOrder() {
        return order;
    }


    private boolean validate(Throwable throwable, RetryerInfo retryerInfo) {

        Set<Class<? extends Throwable>> exclude = retryerInfo.getExclude();
        Set<Class<? extends Throwable>> include = retryerInfo.getInclude();

        if (CollectionUtils.isEmpty(include) && CollectionUtils.isEmpty(exclude)) {
            return true;
        }

        for (Class<? extends Throwable> e : include) {
            if (e.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }

        if (CollectionUtils.isNotEmpty(exclude)) {
            for (Class<? extends Throwable> e : exclude) {
                if (e.isAssignableFrom(throwable.getClass())) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}

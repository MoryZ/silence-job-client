package com.old.silence.job.client.retry.core.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.retry.core.Report;
import com.old.silence.job.client.retry.core.RetryExecutor;
import com.old.silence.job.client.retry.core.RetryExecutorParameter;
import com.old.silence.job.client.retry.core.event.SilenceJobListener;
import com.old.silence.job.client.retry.core.executor.GuavaRetryExecutor;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.loader.SilenceRetrySpiLoader;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.common.util.NetUtil;
import com.old.silence.job.log.SilenceJobLog;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static com.old.silence.job.common.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;



public abstract class AbstractRetryStrategies implements RetryStrategy {


    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String TEXT_MESSAGE_FORMATTER =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 重试组件异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";
    private static final Logger log = LoggerFactory.getLogger(AbstractRetryStrategies.class);

    private final List<SilenceJobListener> silenceJobListeners = SilenceRetrySpiLoader.loadSilenceJobListener();

    @Autowired
    private List<Report> reports;
    @Autowired
    private SilenceJobProperties silenceJobProperties;

    @Override
    public RetryerResultContext openRetry(String sceneName, String executorClassName, Object[] params) {

        RetryerResultContext retryerResultContext = new RetryerResultContext();

        // 开始内存重试
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor(sceneName, executorClassName);
        RetryerInfo retryerInfo = retryExecutor.getRetryerInfo();

        if (!preValidator(retryerInfo, retryerResultContext)) {
            return retryerResultContext;
        }

        RetrySiteSnapshot.setStatus(RetrySiteSnapshot.EnumStatus.RUNNING.getStatus());

        setStage();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter(retryerInfo));

        retryerResultContext.setRetryerInfo(retryerInfo);

        try {
            for (SilenceJobListener silenceJobListener : silenceJobListeners) {
                silenceJobListener.beforeRetry(sceneName, executorClassName, params);
            }

            Object result = retryExecutor.call(retryer, doGetCallable(retryExecutor, params), getRetryErrorConsumer(retryerResultContext, params), getRetrySuccessConsumer(retryerResultContext));
            retryerResultContext.setResult(result);

        } catch (Exception e) {
            log.error("重试期间发生非预期异常, sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName, e);
            retryerResultContext.setMessage("非预期异常" + e.getMessage());
            // 本地重试状态为失败 远程重试状态为成功
            unexpectedError(e, retryerResultContext);

            // 预警
            sendMessage(e);
        } finally {
            // 重试调度完成
            RetrySiteSnapshot.setStatus(RetrySiteSnapshot.EnumStatus.COMPLETE.getStatus());
        }

        return retryerResultContext;
    }

    protected abstract void setStage();

    protected Consumer<Object> getRetrySuccessConsumer(RetryerResultContext retryerResultContext) {
        return o -> {

            success(retryerResultContext);

            Object result = retryerResultContext.getResult();
            RetryerInfo retryerInfo = retryerResultContext.getRetryerInfo();

            for (SilenceJobListener silenceJobListener : silenceJobListeners) {
                silenceJobListener.successOnRetry(result, retryerInfo.getScene(), retryerInfo.getExecutorClassName());
            }

            doRetrySuccessConsumer(retryerResultContext).accept(retryerResultContext);
        };
    }


    protected abstract Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context);

    private Consumer<Throwable> getRetryErrorConsumer(RetryerResultContext context, Object... params) {
        return throwable -> {
            context.setThrowable(throwable);
            context.setMessage(throwable.getMessage());

            error(context);

            RetryerInfo retryerInfo = context.getRetryerInfo();
            try {
                for (SilenceJobListener silenceJobListener : silenceJobListeners) {
                    silenceJobListener
                            .failureOnRetry(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), throwable);
                }
            } catch (Exception e) {
                log.error("失败监听者模式 处理失败 ", e);
                throw e;
            }

            doGetRetryErrorConsumer(retryerInfo, params).accept(throwable);

        };
    }

    protected abstract void error(RetryerResultContext context);

    protected abstract boolean preValidator(RetryerInfo retryerInfo, RetryerResultContext resultContext);

    protected abstract void unexpectedError(Exception e, RetryerResultContext retryerResultContext);

    protected abstract void success(RetryerResultContext retryerResultContext);

    protected abstract Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params);

    protected abstract Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object[] params);

    protected abstract RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo);

    /**
     * 上报数据
     *
     * @param retryerInfo 定义重试场景的信息
     * @param params      执行参数
     */
    protected boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        for (Report report : reports) {
            if (report.supports(retryerInfo.isAsync())) {
                return report.report(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            }
        }

        return Boolean.FALSE;
    }

    private void sendMessage(Exception e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(RetryNotifyScene.CLIENT_COMPONENT_ERROR);
            if (Objects.nonNull(notify)) {
                List<ConfigDTO.Notify.Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(new ArrayList<>());

                for (ConfigDTO.Notify.Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(TEXT_MESSAGE_FORMATTER,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    silenceJobProperties.getNamespace(),
                                    silenceJobProperties.getGroup(),
                                    Instant.now().atZone(ZoneId.systemDefault()).format(formatter),
                                    e.getMessage())
                            .title("retry component handling exception:[{}]", silenceJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());
                    Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
                }
            }
        } catch (Exception e1) {
            SilenceJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }

    }
}

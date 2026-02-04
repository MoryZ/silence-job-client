package com.old.silence.job.client.retry.core.report;

import com.alibaba.fastjson2.JSON;
import com.github.rholder.retry.*;
import com.old.silence.job.client.common.NettyClient;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;

import com.old.silence.job.client.retry.core.RetryExecutor;
import com.old.silence.job.client.retry.core.RetryExecutorParameter;
import com.old.silence.job.client.retry.core.executor.GuavaRetryExecutor;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.common.util.NetUtil;
import com.old.silence.job.common.window.Listener;
import com.old.silence.job.log.SilenceJobLog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.old.silence.job.common.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * 批量异步上报
 *
 */

public class ReportListener implements Listener<RetryTaskDTO> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
    private static final String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 异步批量上报异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, SilenceJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult -> SilenceJobLog.LOCAL.info("Data report successfully requestId:[{}]", nettyResult.getReqId())).build();

    @Override
    public void handler(List<RetryTaskDTO> list) {
        RetryExecutor<WaitStrategy, StopStrategy> retryExecutor =
                new GuavaRetryExecutor();

        Retryer retryer = retryExecutor.build(getRetryExecutorParameter());

        try {
            retryExecutor.call(retryer, () -> {
                SilenceJobLog.LOCAL.info("Batch asynchronous reporting ... <|>{}<|>", JSON.toJSONString(list));
                CLIENT.reportRetryInfo(list);
                return null;
            }, throwable -> {
                SilenceJobLog.LOCAL.error("Data report failed. <|>{}<|>", JSON.toJSONString(list));
                sendMessage(throwable);
            }, o -> SilenceJobLog.LOCAL.info("Data report successful retry：<|>{}<|>", JSON.toJSONString(list)));
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("Data report failed. <|>{}<|>", JSON.toJSONString(list), e);
        }
    }

    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter() {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(2, TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(10);
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {

                        if (attempt.hasException()) {
                            SilenceJobLog.LOCAL.error("SilenceJob上报异常数据时接口发生异常，第[{}]次尝试上报 ", attempt.getAttemptNumber(), attempt.getExceptionCause());
                        }

                    }
                });
            }

        };
    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(RetryNotifyScene.CLIENT_REPORT_ERROR);
            if (Objects.isNull(notify)) {
                return;
            }

            SilenceJobProperties properties = SilenceSpringContext.getBean(SilenceJobProperties.class);
            if (Objects.isNull(properties)) {
                return;
            }
            List<ConfigDTO.Notify.Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(new ArrayList<>());
            for (ConfigDTO.Notify.Recipient recipient : recipients) {
                AlarmContext context = AlarmContext.build()
                        .text(reportErrorTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                NetUtil.getLocalIpStr(),
                                properties.getNamespace(),
                                properties.getGroup(),
                                Instant.now().atZone(ZoneId.systemDefault()).format(formatter),
                                e.getMessage())
                        .title("上报异常:[{}]", properties.getGroup())
                        .notifyAttribute(recipient.getNotifyAttribute());
                Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
            }

        } catch (Exception e1) {
            SilenceJobLog.LOCAL.error("客户端发送组件异常告警失败", e1);
        }

    }
}

package com.old.silence.job.client.retry.core.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.NettyClient;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.enums.RetryNotifyScene;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.common.util.NetUtil;
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
 * 同步上报数据
 *
 */
@Component

public class SyncReport extends AbstractReport {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    private static final String reportErrorTextMessageFormatter =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 同步上报异常</font>  \n" +
                    "> IP:{}  \n" +
                    "> 空间ID:{}  \n" +
                    "> 名称:{}  \n" +
                    "> 时间:{}  \n" +
                    "> 异常:{}  \n";

    @Autowired
    private SilenceJobProperties silenceJobProperties;

    @Override
    public boolean supports(boolean async) {
        return !async;
    }

    @Override
    public boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        return syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params, retryerInfo.getTimeout(), retryerInfo.getUnit());
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReport(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);

        NettyClient client = RequestBuilder.<NettyClient, SilenceJobRpcResult>newBuilder()
                .client(NettyClient.class)
                .async(Boolean.FALSE)
                .timeout(timeout)
                .unit(unit)
                .build();

        try {
            SilenceJobRpcResult result = client.reportRetryInfo(Collections.singletonList(retryTaskDTO));
            SilenceJobLog.LOCAL.debug("Data report result result:[{}]", JSON.toJSONString(result));
            return (Boolean) result.getData();
        } catch (Exception e) {
            sendMessage(e);
            throw e;
        }

    }

    private void sendMessage(Throwable e) {

        try {
            ConfigDTO.Notify notify = GroupVersionCache.getRetryNotifyAttribute(RetryNotifyScene.CLIENT_REPORT_ERROR);
            if (Objects.isNull(notify)) {
                return;
            }

            List<ConfigDTO.Notify.Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(new ArrayList<>());
            for (ConfigDTO.Notify.Recipient recipient : recipients) {
                AlarmContext context = AlarmContext.build()
                        .text(reportErrorTextMessageFormatter,
                                EnvironmentUtils.getActiveProfile(),
                                NetUtil.getLocalIpStr(),
                                silenceJobProperties.getNamespace(),
                                silenceJobProperties.getGroup(),
                                Instant.now().atZone(ZoneId.systemDefault()).format(formatter),
                                e.getMessage())
                        .title("同步上报异常:[{}]", silenceJobProperties.getGroup())
                        .notifyAttribute(recipient.getNotifyAttribute());

                Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
            }

        } catch (Exception e1) {
            SilenceJobLog.LOCAL.error("客户端发送组件异常告警失败", e1);
        }

    }

}

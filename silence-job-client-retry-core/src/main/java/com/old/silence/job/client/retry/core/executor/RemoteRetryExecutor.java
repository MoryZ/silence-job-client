package com.old.silence.job.client.retry.core.executor;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.retry.core.context.RemoteRetryContext;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.log.RetryLogMeta;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;
import com.old.silence.job.client.retry.core.strategy.RetryStrategy;
import com.old.silence.job.common.client.dto.DispatchRetryResultDTO;
import com.old.silence.job.common.enums.RetryResultStatus;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.enums.LogTypeEnum;

import java.util.Objects;


@Component
public class RemoteRetryExecutor {
    private final RetryStrategy remoteRetryStrategies;

    public RemoteRetryExecutor(RetryStrategy remoteRetryStrategies) {
        this.remoteRetryStrategies = remoteRetryStrategies;
    }

    /**
     * 执行远程重试
     *
     * @param context 重试上下文
     */
    public DispatchRetryResultDTO doRetry(RemoteRetryContext context) {
        DispatchRetryResultDTO executeRespDto = new DispatchRetryResultDTO();
        executeRespDto.setRetryId(context.getRetryId());
        executeRespDto.setRetryTaskId(context.getRetryTaskId());
        executeRespDto.setNamespaceId(context.getNamespaceId());
        executeRespDto.setGroupName(context.getGroupName());
        executeRespDto.setSceneName(context.getScene());

        try {
            RetrySiteSnapshot.setAttemptNumber(context.getRetryCount());

            // 初始化实时日志上下文
            initLogContext(context);

            RetryerResultContext retryerResultContext = remoteRetryStrategies.openRetry(context.getScene(),
                    context.getExecutorName(), context.getDeSerialize());

            if (RetrySiteSnapshot.isRetryForStatusCode()) {
                executeRespDto.setStatusCode(RetryResultStatus.STOP.getValue().intValue());
                executeRespDto.setExceptionMsg("下游标记不需要重试");
            } else {
                RetryResultStatus retryResultStatus = retryerResultContext.getRetryResultStatus();
                if (Objects.isNull(retryResultStatus)) {
                    retryResultStatus = RetryResultStatus.STOP;
                    retryerResultContext.setMessage("未获取重试状态. 任务停止");
                }

                executeRespDto.setStatusCode(retryResultStatus.getValue().intValue());
                executeRespDto.setExceptionMsg(retryerResultContext.getMessage());
            }

            if (Objects.nonNull(retryerResultContext.getResult())) {
                executeRespDto.setResultJson(JSON.toJSONString(retryerResultContext.getResult()));
            }

            if (Objects.equals(RetryResultStatus.SUCCESS.getValue().intValue(), executeRespDto.getStatusCode())) {
                SilenceJobLog.REMOTE.info("remote retry【SUCCESS】. count:[{}] result:[{}]", context.getRetryCount(),
                        executeRespDto.getResultJson());
            } else if (Objects.equals(RetryResultStatus.STOP.getValue().intValue(), executeRespDto.getStatusCode())) {
                SilenceJobLog.REMOTE.warn("remote retry 【STOP】. count:[{}] exceptionMsg:[{}]",
                        context.getRetryCount(), executeRespDto.getExceptionMsg());
            } else if (Objects.equals(RetryResultStatus.FAILURE.getValue().intValue(), executeRespDto.getStatusCode())) {
                SilenceJobLog.REMOTE.error("remote retry 【FAILURE】. count:[{}] ", context.getRetryCount(),
                        retryerResultContext.getThrowable());
            } else {
                SilenceJobLog.REMOTE.error("remote retry 【UNKNOWN】. count:[{}] result:[{}]", context.getRetryCount(),
                        executeRespDto.getResultJson(), retryerResultContext.getThrowable());
            }

        } finally {
            RetrySiteSnapshot.removeAll();
            SilenceJobLogManager.removeAll();
        }

        return executeRespDto;
    }

    private static void initLogContext(RemoteRetryContext context) {
        RetryLogMeta retryLogMeta = new RetryLogMeta();
        retryLogMeta.setGroupName(context.getGroupName());
        retryLogMeta.setNamespaceId(context.getNamespaceId());
        retryLogMeta.setRetryId(context.getRetryId());
        retryLogMeta.setRetryTaskId(context.getRetryTaskId());
        SilenceJobLogManager.initLogInfo(retryLogMeta, LogTypeEnum.RETRY);
    }
}

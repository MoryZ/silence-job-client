package com.old.silence.job.client.retry.core.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.old.silence.job.client.retry.core.RetryExecutor;
import com.old.silence.job.client.retry.core.RetryExecutorParameter;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.retryer.RetryType;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;
import com.old.silence.job.common.enums.RetryResultStatus;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 执行本地重试
 *
 */
@Component

public class LocalRetryStrategies extends AbstractRetryStrategies {


    private static final Logger log = LoggerFactory.getLogger(LocalRetryStrategies.class);

    @Override
    public boolean supports(int stage, RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.LOCAL.getStage() == stage;
    }

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.LOCAL.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context) {
        return o -> {

            RetryerInfo retryerInfo = context.getRetryerInfo();
            if (retryerInfo.getRetryType() == RetryType.ONLY_REMOTE) {
                // 若是远程重试模式，采用直接上报服务端
                // 这里标志结果为失败，是表示数据并未重试成功，等待服务端重试
                context.setRetryResultStatus(RetryResultStatus.FAILURE);
            } else {
                SilenceJobLog.LOCAL.debug("doRetrySuccessConsumer 重试成功");
            }
        };
    }

    @Override
    protected void error(RetryerResultContext context) {
        context.setRetryResultStatus(RetryResultStatus.FAILURE);
    }

    @Override
    protected boolean preValidator(RetryerInfo retryerInfo, RetryerResultContext resultContext) {
        if (RetrySiteSnapshot.isRunning()) {
            resultContext.setRetryResultStatus(RetryResultStatus.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 存在正在运行的重试任务");
            return false;
        }

        if (RetrySiteSnapshot.isRetryForStatusCode()) {
            resultContext.setRetryResultStatus(RetryResultStatus.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 下游标志禁止重试");
            return false;
        }

        return true;
    }

    @Override
    protected void unexpectedError(Exception e, RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatus(RetryResultStatus.FAILURE);
    }

    @Override
    protected void success(RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatus(RetryResultStatus.SUCCESS);
    }

    @Override
    public Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params) {
        return throwable -> {
            // 执行上报服务端
            log.info("内存重试完成且异常未被解决 scene:[{}]", retryerInfo.getScene());
            // 上报服务端异常
            if (RetryType.LOCAL_REMOTE.name().equals(retryerInfo.getRetryType().name())) {
                // 上报
                log.debug("上报 scene:[{}]", retryerInfo.getScene());
                doReport(retryerInfo, params);
            }
        };
    }

    @Override
    public Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object... params) {

        RetryerInfo retryerInfo = retryExecutor.getRetryerInfo();
        RetryType retryType = retryerInfo.getRetryType();
        switch (retryType) {
            // 如果是仅仅本地重试或本地_远程模式则先支持重试
            case ONLY_LOCAL:
            case LOCAL_REMOTE:

                // 标记进入方法的时间
                RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());

                return () -> retryExecutor.execute(params);
            case ONLY_REMOTE:
                // 仅仅是远程重试则直接上报
                log.debug("上报 scene:[{}]", retryerInfo.getScene());
                doReport(retryerInfo, params);
                RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.REMOTE.getStage());
                return () -> null;
            default:
                throw new SilenceRetryClientException("异常重试模式 [{}]", retryType.name());
        }

    }

    @Override
    public RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {

        return new RetryExecutorParameter<>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(retryerInfo.getLocalInterval(), TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(retryerInfo.getLocalTimes());
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        if (attempt.hasException()) {
                            RetryType retryType = retryerInfo.getRetryType();
                            switch (retryType) {
                                case ONLY_LOCAL:
                                case LOCAL_REMOTE:
                                    SilenceJobLog.LOCAL.error("[{}] 本地重试执行失败，第[{}]次重试", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                case ONLY_REMOTE:
                                    SilenceJobLog.LOCAL.error("[{}] 上报服务端执行失败，第[{}]次重试", retryerInfo.getScene(), attempt.getAttemptNumber());
                                    break;
                                default:
                                    throw new SilenceRetryClientException("异常重试模式 [{}]", retryType.name());

                            }
                        } else {
                            RetryType retryType = retryerInfo.getRetryType();
                            switch (retryType) {
                                case ONLY_LOCAL:
                                case LOCAL_REMOTE:
                                    SilenceJobLog.LOCAL.info("[{}] 本地重试执行成功.", retryerInfo.getScene());
                                    break;
                                case ONLY_REMOTE:
                                    SilenceJobLog.LOCAL.info("[{}] 上报服务端执行成功.", retryerInfo.getScene());
                                    break;
                                default:
                                    throw new SilenceRetryClientException("异常重试模式 [{}]", retryType.name());

                            }
                        }

                    }
                });
            }

        };
    }
}

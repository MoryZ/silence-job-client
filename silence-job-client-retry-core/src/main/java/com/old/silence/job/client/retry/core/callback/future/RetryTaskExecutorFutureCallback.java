package com.old.silence.job.client.retry.core.callback.future;

import com.google.common.util.concurrent.FutureCallback;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;

import com.old.silence.job.client.retry.core.client.RetryClient;
import com.old.silence.job.client.retry.core.context.RemoteRetryContext;
import com.old.silence.job.common.client.dto.DispatchRetryResultDTO;
import com.old.silence.job.common.client.dto.request.DispatchRetryResultRequest;
import com.old.silence.job.common.enums.RetryResultStatus;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Objects;
import java.util.concurrent.CancellationException;

/**
 * <p>
 * 重试执行结果上报
 * </p>
 *
 */
public class RetryTaskExecutorFutureCallback implements FutureCallback<DispatchRetryResultDTO> {

    private static final RetryClient CLIENT = RequestBuilder.<RetryClient, SilenceJobRpcResult>newBuilder()
            .client(RetryClient.class)
            .callback(nettyResult -> {
                if (nettyResult.getCode() == 500) {

                    SilenceJobLog.LOCAL.error("Retry execute result report successfully requestId:[{}]",
                            nettyResult.getReqId());
                }

            }).build();


    private final RemoteRetryContext retryContext;
    public RetryTaskExecutorFutureCallback(RemoteRetryContext retryContext) {
        this.retryContext = retryContext;
    }

    @Override
    public void onSuccess(DispatchRetryResultDTO result) {

        try {
            DispatchRetryResultRequest request = buildDispatchRetryResultRequest(result);
            if (RetryResultStatus.SUCCESS.getValue().intValue() == result.getStatusCode()) {
                request.setTaskStatus(RetryTaskStatus.SUCCESS);
            } else if (RetryResultStatus.STOP.getValue().intValue() == result.getStatusCode()) {
                request.setTaskStatus(RetryTaskStatus.STOP);
            } else {
                request.setTaskStatus(RetryTaskStatus.FAIL);
            }
            CLIENT.dispatchResult(request);
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("执行结果上报异常.[{}]", retryContext.getRetryTaskId(), e);
        }

    }


    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            SilenceJobLog.LOCAL.debug("任务已经被取消，不做状态回传");
            return;
        }

        try {
            DispatchRetryResultRequest request = buildDispatchRetryResultRequest(null);
            request.setExceptionMsg(t.getMessage());
            request.setTaskStatus(RetryTaskStatus.FAIL);
            CLIENT.dispatchResult(request);
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("执行结果上报异常.[{}]", retryContext.getRetryTaskId(), e);
        }

    }


    private DispatchRetryResultRequest buildDispatchRetryResultRequest(DispatchRetryResultDTO result) {
        DispatchRetryResultRequest request = new DispatchRetryResultRequest();
        request.setRetryTaskId(retryContext.getRetryTaskId());
        request.setNamespaceId(retryContext.getNamespaceId());
        request.setGroupName(retryContext.getGroupName());
        request.setSceneName(retryContext.getScene());
        request.setRetryId(retryContext.getRetryId());
        request.setRetryTaskId(retryContext.getRetryTaskId());
        if (Objects.nonNull(result)) {
            request.setResult(result.getResultJson());
            request.setExceptionMsg(result.getExceptionMsg());
        }
        return request;
    }
}

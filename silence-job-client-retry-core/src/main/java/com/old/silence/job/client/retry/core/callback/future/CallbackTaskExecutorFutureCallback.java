package com.old.silence.job.client.retry.core.callback.future;

import com.google.common.util.concurrent.FutureCallback;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.client.retry.core.client.RetryClient;
import com.old.silence.job.client.retry.core.context.CallbackContext;
import com.old.silence.job.common.client.dto.request.DispatchCallbackResultRequest;
import com.old.silence.job.common.enums.RetryTaskStatus;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Objects;
import java.util.concurrent.CancellationException;


public class CallbackTaskExecutorFutureCallback implements FutureCallback<Boolean> {

    private static final RetryClient CLIENT = RequestBuilder.<RetryClient, SilenceJobRpcResult>newBuilder()
            .client(RetryClient.class)
            .callback(nettyResult -> {
                if (Objects.equals(nettyResult.getCode(), 500)) {
                    SilenceJobLog.LOCAL.error("Retry callback execute result report successfully requestId:[{}]",
                            nettyResult.getReqId());
                }

            }).build();


    private final CallbackContext context;
    public CallbackTaskExecutorFutureCallback(CallbackContext context) {
        this.context = context;
    }

    @Override
    public void onSuccess(Boolean result) {
        try {
            DispatchCallbackResultRequest request = buildDispatchRetryResultRequest();
            request.setTaskStatus(RetryTaskStatus.SUCCESS);
            CLIENT.callbackResult(request);
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("回调执行结果上报异常.[{}]", context.getRetryTaskId(), e);

        }

    }

    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            SilenceJobLog.LOCAL.debug("任务已经被取消，不做状态回传");
            return;
        }
        try {
            DispatchCallbackResultRequest request = buildDispatchRetryResultRequest();
            request.setTaskStatus(RetryTaskStatus.FAIL);
            request.setExceptionMsg(t.getMessage());
            CLIENT.callbackResult(request);
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("回调执行结果上报异常.[{}]", context.getRetryTaskId(), e);
        }
    }

    private DispatchCallbackResultRequest buildDispatchRetryResultRequest() {
        DispatchCallbackResultRequest request = new DispatchCallbackResultRequest();
        request.setRetryTaskId(context.getRetryTaskId());
        request.setNamespaceId(context.getNamespaceId());
        request.setGroupName(context.getGroupName());
        request.setSceneName(context.getSceneName());
        request.setRetryId(context.getRetryId());
        request.setRetryTaskId(context.getRetryTaskId());
        return request;
    }
}

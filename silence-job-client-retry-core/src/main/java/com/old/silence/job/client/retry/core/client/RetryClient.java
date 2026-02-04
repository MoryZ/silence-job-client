package com.old.silence.job.client.retry.core.client;

 
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.common.client.dto.request.DispatchCallbackResultRequest;
import com.old.silence.job.common.client.dto.request.DispatchRetryResultRequest;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.ApiResult;

/**
 * netty 客户端请求类
 *
 */
public interface RetryClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_RETRY_DISPATCH_RESULT)
    ApiResult dispatchResult(DispatchRetryResultRequest request);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_CALLBACK_RESULT)
    ApiResult callbackResult(DispatchCallbackResultRequest request);
}

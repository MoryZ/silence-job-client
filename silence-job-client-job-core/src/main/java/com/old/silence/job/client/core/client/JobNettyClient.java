package com.old.silence.job.client.core.client;


import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.common.client.dto.request.DispatchJobResultRequest;
import com.old.silence.job.common.client.dto.request.MapTaskRequest;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.ApiResult;

/**
 * netty 客户端请求类
 *
 */
public interface JobNettyClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.REPORT_JOB_DISPATCH_RESULT)
    ApiResult dispatchResult(DispatchJobResultRequest request);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT_JOB_MAP_TASK)
    ApiResult<Boolean> batchReportMapTask(MapTaskRequest mapTaskRequest);
}

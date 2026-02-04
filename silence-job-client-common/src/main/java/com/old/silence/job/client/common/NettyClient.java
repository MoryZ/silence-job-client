package com.old.silence.job.client.common;

import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.LogTaskDTO;
import com.old.silence.job.common.server.dto.RetryTaskDTO;

import java.util.List;


/**
 * netty 客户端请求类
 *
 */
public interface NettyClient {

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_REPORT)
    SilenceJobRpcResult reportRetryInfo(List<RetryTaskDTO> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BATCH_LOG_REPORT)
    SilenceJobRpcResult reportLogTask(List<LogTaskDTO> list);

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.SYNC_CONFIG)
    ApiResult syncRemoteConfig();

    @Mapping(method = RequestMethod.POST, path = HTTP_PATH.BEAT)
    ApiResult beat(String mark);

}

package com.old.silence.job.client.retry.core.plugin;

import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.model.SilenceJobHeaders;
import com.old.silence.job.log.SilenceJobLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;



public class RequestHeaderPlugins {

    private RequestHeaderPlugins() {
    }

    /**
     * 请求头传递
     *
     * @return 头信息
     */
    public static Map<String, String> requestHeader() {

        Map<String, String> header = new HashMap<>();
        SilenceJobHeaders retryHeader = RetrySiteSnapshot.getRetryHeader();

        // 传递请求头
        if (Objects.nonNull(retryHeader)) {
            long callRemoteTime = System.currentTimeMillis();
            Long entryMethodTime = RetrySiteSnapshot.getEntryMethodTime();
            if (Objects.isNull(entryMethodTime)) {
                SilenceJobLog.LOCAL.warn("entry method time is null. retryId:[{}]", retryHeader.getRetryId());
            } else {
                long transmitTime = retryHeader.getDdl() - (callRemoteTime - entryMethodTime);
                SilenceJobLog.LOCAL.info("RPC传递header头 callRemoteTime:[{}] - entryMethodTime:[{}] = transmitTime:[{}]", callRemoteTime, entryMethodTime, transmitTime);
                if (transmitTime > 0) {
                    retryHeader.setDdl(transmitTime);
                    // 重新刷新进入时间
                    RetrySiteSnapshot.setEntryMethodTime(System.currentTimeMillis());
                } else {
                    throw new SilenceRetryClientException("调用链超时, 不在继续调用后面请求");
                }
            }

            header.put(SystemConstants.SILENCE_JOB_HEAD_KEY, JSON.toJSONString(retryHeader));
        }

        return header;
    }

}

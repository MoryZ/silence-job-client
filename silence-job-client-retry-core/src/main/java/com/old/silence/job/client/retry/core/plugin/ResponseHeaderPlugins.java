package com.old.silence.job.client.retry.core.plugin;

import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.common.constant.SystemConstants;

import java.util.List;
import java.util.Map;


public class ResponseHeaderPlugins {

    private ResponseHeaderPlugins() {
    }

    /**
     * 获取接口返回的响应头
     *
     * @param header 响应头
     */
    public static void responseHeader(Map<String, List<String>> header) {

        // 获取不重试标志
        if (header.containsKey(SystemConstants.SILENCE_JOB_STATUS_CODE_KEY)) {
            List<String> statusCode = header.get(SystemConstants.SILENCE_JOB_STATUS_CODE_KEY);
            RetrySiteSnapshot.setRetryStatusCode(statusCode.get(0));
        }
    }

}

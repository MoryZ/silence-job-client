package com.old.silence.job.client.core.executor.builtin;


import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.core.annotation.JobExecutor;
import com.old.silence.job.client.core.dto.JobArgs;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.constant.SystemConstants;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@JobExecutor(name = "silenceJobHttpExecutor")
public class SilenceJobHttpExecutor extends AbstractHttpExecutor {

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        Object jobParams = jobArgs.getJobParams();
        HttpParams httpParams = JSON.parseObject((String) jobParams, HttpParams.class);
        if (Objects.nonNull(jobArgs.getWfContext())) {
            httpParams.setWfContext(jobArgs.getWfContext());
        }
        httpParams.setMethod(httpParams.getMethod().toUpperCase());
        Map<String, String> hashMap = new HashMap<>(3);
        hashMap.put(SystemConstants.SILENCE_JOB_CLIENT_GROUP, SILENCE_JOB_PROPERTIES.getGroup());
        hashMap.put(SystemConstants.SILENCE_JOB_CLIENT_GROUP_TOKEN, SILENCE_JOB_PROPERTIES.getToken());
        hashMap.put(SystemConstants.SILENCE_JOB_CLIENT_NAMESPACE, SILENCE_JOB_PROPERTIES.getNamespace());
        Map<String, String> headers = (Objects.isNull(httpParams.getHeaders()) || httpParams.getHeaders().isEmpty()) ? new HashMap<>() : httpParams.getHeaders();
        headers.putAll(hashMap);
        httpParams.setHeaders(headers);
        return process(httpParams);
    }

}

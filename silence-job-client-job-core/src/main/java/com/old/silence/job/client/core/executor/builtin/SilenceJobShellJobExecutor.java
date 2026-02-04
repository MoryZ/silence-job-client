package com.old.silence.job.client.core.executor.builtin;


import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.core.annotation.JobExecutor;
import com.old.silence.job.client.core.dto.JobArgs;
import com.old.silence.job.common.client.dto.ExecuteResult;
import org.springframework.stereotype.Component;

@Component
@JobExecutor(name = "silenceJobShellJobExecutor")
public class SilenceJobShellJobExecutor extends AbstractShellExecutor {

    public ExecuteResult jobExecute(JobArgs jobArgs) {
        Object jobParams = jobArgs.getJobParams();
        ScriptParams scriptParams = JSON.parseObject((String) jobParams, ScriptParams.class);
        return process(jobArgs.getJobId(), scriptParams);
    }

}

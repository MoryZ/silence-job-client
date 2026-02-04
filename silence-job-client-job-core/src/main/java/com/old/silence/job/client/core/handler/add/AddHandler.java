package com.old.silence.job.client.core.handler.add;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.handler.AbstractParamsHandler;
import com.old.silence.job.client.core.util.ValidatorUtils;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.model.ApiResult;

import static com.old.silence.job.common.enums.TriggerType.WORK_FLOW;


public abstract class AddHandler<H> extends AbstractParamsHandler<H, Long> {

    public AddHandler(JobTaskType taskType) {
        super(taskType);
    }

    @Override
    protected Long doExecute() {
        ApiResult<Object> result = client.addJob(getReqDTO());
        Assert.isTrue(200 == result.getCode(),
                () -> new SilenceJobClientException(result.getMessage()));
        String data = JSON.toJSONString(result.getData());
        return Long.valueOf(data);
    }

    @Override
    protected void beforeExecute() {
        // 此次是兜底覆盖,工作流是没有调度时间
        if (getReqDTO().getTriggerType() == WORK_FLOW) {
            setTriggerInterval("*");
        }
    }

    @Override
    protected void afterExecute(Long id) {

    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(Add.class, getReqDTO());
    }

}

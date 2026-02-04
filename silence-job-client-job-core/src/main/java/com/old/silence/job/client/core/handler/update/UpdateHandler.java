package com.old.silence.job.client.core.handler.update;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.core.handler.AbstractParamsHandler;
import com.old.silence.job.client.core.util.ValidatorUtils;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;


public abstract class UpdateHandler<H> extends AbstractParamsHandler<H, Boolean> {

    public UpdateHandler(JobTaskType typeEnum, BigInteger jobId) {
        super(typeEnum);
        // 更新必须要id
        setId(jobId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {
        if (getReqDTO().getTriggerType().equals(TriggerType.WORK_FLOW)) {

            // 工作流没有调度时间
            setTriggerInterval("*");
        }
    }

    @Override
    protected Boolean doExecute() {
        ApiResult<Object> result = client.updateJob(getReqDTO());
        Assert.isTrue(Objects.equals(200, result.getCode()),
                () -> new SilenceJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(Update.class, getReqDTO());
    }

}

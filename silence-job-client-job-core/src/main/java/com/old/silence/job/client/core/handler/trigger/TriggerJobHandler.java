package com.old.silence.job.client.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.handler.AbstractTriggerHandler;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;

public abstract class TriggerJobHandler<H> extends AbstractTriggerHandler<H, Boolean> {

    public TriggerJobHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        ApiResult<Object> result = client.triggerJob(getReqDTO());
        Assert.isTrue(Objects.equals(200, result.getCode()),
                () -> new SilenceJobClientException(result.getMessage()));
        return (Boolean)result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getJobId() != null && BigInteger.ZERO.compareTo(getReqDTO().getJobId()) != 0,  "triggerJobId不能为null并且必须大于0");
    }
}

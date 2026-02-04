package com.old.silence.job.client.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.handler.AbstractTriggerHandler;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;

public class TriggerWorkflowHandler extends AbstractTriggerHandler<TriggerWorkflowHandler,Boolean> {


    public TriggerWorkflowHandler(BigInteger triggerJobId) {
        super(triggerJobId);
        setR(this);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        ApiResult<Object> result = client.triggerWorkFlow(getReqDTO());
        Assert.isTrue(Objects.equals(200, result.getCode()),
                () -> new SilenceJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getJobId() != null && BigInteger.ZERO.compareTo(getReqDTO().getJobId()) != 0, "triggerJobId不能为null并且必须大于0");
    }

    @Override
    public TriggerWorkflowHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}

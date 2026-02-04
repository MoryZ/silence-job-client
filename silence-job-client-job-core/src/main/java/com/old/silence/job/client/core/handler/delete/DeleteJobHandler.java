package com.old.silence.job.client.core.handler.delete;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.handler.AbstractRequestHandler;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Set;


public class DeleteJobHandler extends AbstractRequestHandler<Boolean> {
    private final Set<BigInteger> toDeleteIds;

    public DeleteJobHandler(Set<BigInteger> toDeleteIds) {
        this.toDeleteIds = toDeleteIds;
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        ApiResult<Object> result = client.deleteJob(toDeleteIds);
        Assert.isTrue(Objects.equals(200, result.getCode()),
                () -> new SilenceJobClientException(result.getMessage()));
        return (Boolean)result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(toDeleteIds != null && !toDeleteIds.isEmpty() && !toDeleteIds.contains(BigInteger.ZERO),  "toDeleteId不能为null或0");
    }
}

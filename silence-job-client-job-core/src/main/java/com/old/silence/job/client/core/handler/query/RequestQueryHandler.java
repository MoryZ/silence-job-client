package com.old.silence.job.client.core.handler.query;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.dto.JobResponseVO;
import com.old.silence.job.client.core.handler.AbstractRequestHandler;
import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;

public class RequestQueryHandler extends AbstractRequestHandler<JobResponseVO> {
    private final BigInteger queryJobId;

    public RequestQueryHandler(BigInteger queryJobId) {
        this.queryJobId = queryJobId;
    }

    @Override
    protected void afterExecute(JobResponseVO jobResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobResponseVO doExecute() {
        ApiResult<Object> result = client.getJobDetail(queryJobId);
        Assert.isTrue(200 == result.getCode(),
                () -> new SilenceJobClientException(result.getMessage()));
        Object data = result.getData();
        Assert.isTrue(Objects.nonNull(data), () -> new SilenceJobClientException("获取[{}]任务详情失败", queryJobId));
        return JSON.parseObject(JSON.toJSONString(data), JobResponseVO.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(queryJobId != null && BigInteger.ZERO.compareTo(queryJobId) != 0, "queryJobId不能为null并且必须大于0");
    }

}

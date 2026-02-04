package com.old.silence.job.client.core.handler.update;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.dto.RequestUpdateStatusDTO;
import com.old.silence.job.client.core.handler.AbstractRequestHandler;
import com.old.silence.job.client.core.util.ValidatorUtils;

import com.old.silence.job.common.model.ApiResult;

import java.math.BigInteger;
import java.util.Objects;


public class UpdateJobStatusHandler extends AbstractRequestHandler<Boolean> {
    private final RequestUpdateStatusDTO statusDTO;

    public UpdateJobStatusHandler(BigInteger id) {
        this.statusDTO = new RequestUpdateStatusDTO();
        setId(id);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        ApiResult<Object> result = client.updateJobStatus(statusDTO);
        Assert.isTrue(Objects.equals(200, result.getCode()),
                () -> new SilenceJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(statusDTO);
    }

    /**
     * 设置任务/工作流ID
     *
     * @param id 工作流ID
     */
    private UpdateJobStatusHandler setId(BigInteger id) {
        this.statusDTO.setId(id);
        return this;
    }

    /**
     * 设置状态
     *
     */
    public UpdateJobStatusHandler setStatus(Boolean yesOrNoStatus) {
        this.statusDTO.setJobStatus(yesOrNoStatus);
        return this;
    }
}

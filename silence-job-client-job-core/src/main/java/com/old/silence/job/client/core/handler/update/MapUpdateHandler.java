package com.old.silence.job.client.core.handler.update;


import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class MapUpdateHandler extends UpdateHandler<MapUpdateHandler> {

    public MapUpdateHandler(BigInteger jobId) {
        super(JobTaskType.MAP, jobId);
        setR(this);
    }

    @Override
    public MapUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}

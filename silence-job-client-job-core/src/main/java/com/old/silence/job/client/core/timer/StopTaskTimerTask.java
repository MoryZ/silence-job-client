package com.old.silence.job.client.core.timer;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import com.old.silence.job.client.core.cache.ThreadPoolCache;

import java.math.BigInteger;


public class StopTaskTimerTask implements TimerTask {

    private final BigInteger taskBatchId;

    public StopTaskTimerTask(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ThreadPoolCache.stopThreadPool(taskBatchId);
    }
}

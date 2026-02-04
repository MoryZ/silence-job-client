package com.old.silence.job.client.retry.core.retryer;



import com.old.silence.job.client.retry.core.RetryOperations;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.strategy.ExecutorMethod;
import com.old.silence.job.client.retry.core.strategy.RetryStrategy;

import java.util.Objects;

/**
 * 手动生成重试任务模板类
 *
 */
public class SilenceJobTemplate implements RetryOperations {

    private Class<? extends ExecutorMethod> executorMethodClass;
    private String scene;
    private Object[] params;
    private RetryStrategy retryStrategy;

    @Override
    public void executeRetry() {

        Integer stage = RetrySiteSnapshot.getStage();
        try {
            retryStrategy.openRetry(scene, executorMethodClass.getName(), params);
        } finally {
            // stage == null 则非嵌套重试, 需求清除线程记录的数据信息
            // stage != null 则由上层调度的进行清除线程记录的数据信息
            if (Objects.isNull(stage)) {
                RetrySiteSnapshot.removeAll();
            } else {
                // 还原原始的重试阶段
                RetrySiteSnapshot.setStage(stage);
            }
        }
    }

    protected void setExecutorMethodClass(
            Class<? extends ExecutorMethod> executorMethodClass) {
        this.executorMethodClass = executorMethodClass;
    }

    protected void setScene(String scene) {
        this.scene = scene;
    }

    protected void setParams(Object[] params) {
        this.params = params;
    }

    protected void setRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
}

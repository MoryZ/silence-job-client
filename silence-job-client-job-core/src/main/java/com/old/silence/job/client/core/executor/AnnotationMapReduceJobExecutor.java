package com.old.silence.job.client.core.executor;

import com.old.silence.job.client.core.MapHandler;
import com.old.silence.job.client.core.cache.JobExecutorInfoCache;
import com.old.silence.job.client.core.dto.JobExecutorInfo;
import com.old.silence.job.client.core.dto.MapArgs;
import com.old.silence.job.client.core.dto.MergeReduceArgs;
import com.old.silence.job.client.core.dto.ReduceArgs;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.exception.SilenceJobMapReduceException;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 基于注解的MapReduce执行器
 *
 */
@Component
public class AnnotationMapReduceJobExecutor extends AbstractMapReduceExecutor {

    @Override
    protected ExecuteResult doReduceExecute(ReduceArgs reduceArgs) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(reduceArgs.getExecutorInfo());
        if (Objects.isNull(jobExecutorInfo)) {
            throw new SilenceJobMapReduceException("[{}] not found", reduceArgs.getExecutorInfo());
        }

        if (Objects.isNull(jobExecutorInfo.getReduceExecutor())) {
            throw new SilenceJobMapReduceException(
                "[{}] MapTask execution method not found. Please configure the @ReduceExecutor annotation",
                reduceArgs.getExecutorInfo());
        }

        Class<?>[] paramTypes = jobExecutorInfo.getReduceExecutor().getParameterTypes();
        if (paramTypes.length > 0) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getReduceExecutor(),
                jobExecutorInfo.getExecutor(), reduceArgs);
        }

        throw new SilenceJobMapReduceException("[{}] ReduceTask execution method not found", reduceArgs.getExecutorInfo());
    }

    @Override
    protected ExecuteResult doMergeReduceExecute(MergeReduceArgs mergeReduceArgs) {
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(mergeReduceArgs.getExecutorInfo());

        if (Objects.isNull(jobExecutorInfo)) {
            throw new SilenceJobMapReduceException("[{}] not found", mergeReduceArgs.getExecutorInfo());
        }

        Method mergeReduceExecutor = jobExecutorInfo.getMergeReduceExecutor();
        if (Objects.isNull(mergeReduceExecutor)) {
            throw new SilenceJobMapReduceException(
                "[{}] MapTask execution method not found. Please configure the @MergeReduceExecutor annotation",
                mergeReduceArgs.getExecutorInfo());
        }

        Class<?>[] paramTypes = mergeReduceExecutor.getParameterTypes();
        if (paramTypes.length > 0) {
            return (ExecuteResult) ReflectionUtils.invokeMethod(mergeReduceExecutor,
                jobExecutorInfo.getExecutor(), mergeReduceArgs);
        }

        throw new SilenceJobMapReduceException("[{}] MergeReduceTask execution method not found [{}]",
            mergeReduceArgs.getExecutorInfo());

    }

    @Override
    public ExecuteResult doJobMapExecute(MapArgs mapArgs, MapHandler mapHandler) {
        return invokeMapExecute(mapArgs, mapHandler);
    }
}

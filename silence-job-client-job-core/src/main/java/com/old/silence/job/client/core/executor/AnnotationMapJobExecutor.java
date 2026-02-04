package com.old.silence.job.client.core.executor;

import com.old.silence.job.client.core.MapHandler;

import com.old.silence.job.client.core.dto.MapArgs;
import com.old.silence.job.common.client.dto.ExecuteResult;
import org.springframework.stereotype.Component;

/**
 * 基于注解的Map任务执行器
 *
 */
@Component
public class AnnotationMapJobExecutor extends AbstractMapExecutor {

    @Override
    public ExecuteResult doJobMapExecute(MapArgs mapArgs, MapHandler mapHandler) {
        return invokeMapExecute(mapArgs, mapHandler);
    }
}

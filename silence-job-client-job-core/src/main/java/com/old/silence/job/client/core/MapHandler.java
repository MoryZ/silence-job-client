package com.old.silence.job.client.core;


import com.old.silence.job.common.client.dto.ExecuteResult;

import java.util.List;


public interface MapHandler<T> {

    /**
     * 执行MAP操作
     *
     * @param taskList 需要分片的集合(建议不超过200个, 超过500禁止分片.)
     * @param nextTaskName 下一次需要处理MAP的节点名称 (不能是ROOT_MAP)
     * @return ExecuteResult
     */
    ExecuteResult doMap(List<T> taskList, String nextTaskName);
}

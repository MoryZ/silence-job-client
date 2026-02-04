package com.old.silence.job.client.core.openapi;




import com.old.silence.job.client.core.handler.add.BroadcastAddHandler;
import com.old.silence.job.client.core.handler.add.ClusterAddHandler;
import com.old.silence.job.client.core.handler.add.MapAddHandler;
import com.old.silence.job.client.core.handler.add.MapReduceAddHandler;
import com.old.silence.job.client.core.handler.add.ShardingAddHandler;
import com.old.silence.job.client.core.handler.delete.DeleteJobHandler;
import com.old.silence.job.client.core.handler.delete.DeleteWorkflowHandler;
import com.old.silence.job.client.core.handler.query.RequestQueryHandler;
import com.old.silence.job.client.core.handler.trigger.BroadcastTriggerHandler;
import com.old.silence.job.client.core.handler.trigger.ClusterTriggerHandler;
import com.old.silence.job.client.core.handler.trigger.MapReduceTriggerHandler;
import com.old.silence.job.client.core.handler.trigger.MapTriggerHandler;
import com.old.silence.job.client.core.handler.trigger.ShardingTriggerHandler;
import com.old.silence.job.client.core.handler.trigger.TriggerWorkflowHandler;
import com.old.silence.job.client.core.handler.update.BroadcastUpdateHandler;
import com.old.silence.job.client.core.handler.update.ClusterUpdateHandler;
import com.old.silence.job.client.core.handler.update.MapReduceUpdateHandler;
import com.old.silence.job.client.core.handler.update.MapUpdateHandler;
import com.old.silence.job.client.core.handler.update.ShardingUpdateHandler;
import com.old.silence.job.client.core.handler.update.UpdateJobStatusHandler;
import com.old.silence.job.client.core.handler.update.UpdateWorkflowStatusHandler;

import java.math.BigInteger;
import java.util.Set;


public final class SilenceJobOpenApi {
    private SilenceJobOpenApi() {
    }

    /**
     * 添加集群定时任务
     *
     * @return {@link ClusterAddHandler}
     */
    public static ClusterAddHandler addClusterJob() {
        return new ClusterAddHandler();
    }

    /**
     * 添加广播定时任务
     *
     * @return {@link BroadcastAddHandler}
     */
    public static BroadcastAddHandler addBroadcastJob() {
        return new BroadcastAddHandler();
    }

    /**
     * 添加固定分片定时任务
     *
     * @return {@link ShardingAddHandler}
     */
    public static ShardingAddHandler addShardingJob() {
        return new ShardingAddHandler();
    }

    /**
     * 添加Map定时任务
     *
     * @return {@link MapAddHandler}
     */
    public static MapAddHandler addMapJob() {
        return new MapAddHandler();
    }

    /**
     * 添加MapReduce定时任务
     *
     * @return {@link MapReduceAddHandler}
     */
    public static MapReduceAddHandler addMapReduceJob() {
        return new MapReduceAddHandler();
    }

    /**
     * 更新广播定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link BroadcastUpdateHandler}
     */
    public static BroadcastUpdateHandler updateBroadcastJob(BigInteger jobId) {
        return new BroadcastUpdateHandler(jobId);
    }

    /**
     * 更新集群定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ClusterUpdateHandler}
     */
    public static ClusterUpdateHandler updateClusterJob(BigInteger jobId) {
        return new ClusterUpdateHandler(jobId);
    }

    /**
     * 更新MapReduce定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapReduceUpdateHandler}
     */
    public static MapReduceUpdateHandler updateMapReduceJob(BigInteger jobId) {
        return new MapReduceUpdateHandler(jobId);
    }

    /**
     * 更新Map定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapUpdateHandler}
     */
    public static MapUpdateHandler updateMapJob(BigInteger jobId) {
        return new MapUpdateHandler(jobId);
    }

    /**
     * 更新静态分片定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ShardingUpdateHandler}
     */
    public static ShardingUpdateHandler updateShardingJob(BigInteger jobId) {
        return new ShardingUpdateHandler(jobId);
    }

    /**
     * 获取定时任务详情
     *
     * @param jobId 定时任务ID
     * @return {@link RequestQueryHandler}
     */
    public static RequestQueryHandler getJobDetail(BigInteger jobId) {
        return new RequestQueryHandler(jobId);
    }

    /**
     * 手动触发广播定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link BroadcastTriggerHandler}
     */
    public static BroadcastTriggerHandler triggerBroadcastJob(BigInteger jobId) {
        return new BroadcastTriggerHandler(jobId);
    }

    /**
     * 手动触发集群定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ClusterTriggerHandler}
     */
    public static ClusterTriggerHandler triggerClusterJob(BigInteger jobId) {
        return new ClusterTriggerHandler(jobId);
    }

    /**
     * 手动触发MapReduce定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapReduceTriggerHandler}
     */
    public static MapReduceTriggerHandler triggerMapReduceJob(BigInteger jobId) {
        return new MapReduceTriggerHandler(jobId);
    }

    /**
     * 手动触发Map定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapTriggerHandler}
     */
    public static MapTriggerHandler triggerMapJob(BigInteger jobId) {
        return new MapTriggerHandler(jobId);
    }


    /**
     * 手动触发静态分片定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ShardingTriggerHandler}
     */
    public static ShardingTriggerHandler triggerShardingJob(BigInteger jobId) {
        return new ShardingTriggerHandler(jobId);
    }



    /**
     * 手动触发工作流任务
     *
     * @param id 工作流任务ID
     * @return {@link TriggerWorkflowHandler}
     */
    public static TriggerWorkflowHandler triggerWorkFlow(BigInteger id) {
        return new TriggerWorkflowHandler(id);
    }

    /**
     * 更新定时任务状态
     *
     * @param jobId 任务ID
     * @return {@link UpdateJobStatusHandler}
     */
    public static UpdateJobStatusHandler updateJobStatus(BigInteger jobId) {
        return new UpdateJobStatusHandler(jobId);
    }

    /**
     * 更新工作流任务状态
     *
     * @param workFlowId 工作流ID
     * @return {@link UpdateJobStatusHandler}
     */
    public static UpdateWorkflowStatusHandler updateWorkFlowStatus(BigInteger workFlowId) {
        return new UpdateWorkflowStatusHandler(workFlowId);
    }

    /**
     * 删除任务
     *
     * @param toDeleteIds 待删除任务IDS
     * @return {@link DeleteJobHandler}
     */
    public static DeleteJobHandler deleteJob(Set<BigInteger> toDeleteIds){
        return new DeleteJobHandler(toDeleteIds);
    }

    /**
     * 删除工作流任务
     *
     * @param toDeleteIds 待删除工作流任务IDS
     * @return {@link DeleteWorkflowHandler}
     */
    public static DeleteWorkflowHandler deleteWorkflow(Set<BigInteger> toDeleteIds){
        return new DeleteWorkflowHandler(toDeleteIds);
    }
}

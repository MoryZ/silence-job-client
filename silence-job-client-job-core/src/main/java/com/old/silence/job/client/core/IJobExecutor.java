package com.old.silence.job.client.core;


import com.old.silence.job.common.model.JobContext;

/**
 * job执行者
 *
 */
public interface IJobExecutor {

    void jobExecute(JobContext jobContext);
}

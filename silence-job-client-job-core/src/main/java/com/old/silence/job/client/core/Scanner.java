package com.old.silence.job.client.core;


import com.old.silence.job.client.core.dto.JobExecutorInfo;

import java.util.List;

/**
 * @author moryzang
 */
public interface Scanner {

    List<JobExecutorInfo> doScan();
}
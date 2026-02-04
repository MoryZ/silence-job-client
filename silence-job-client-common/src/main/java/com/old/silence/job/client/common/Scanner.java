package com.old.silence.job.client.common;

import com.old.silence.job.client.common.rpc.supports.scan.EndPointInfo;

import java.util.List;


public interface Scanner {

    List<EndPointInfo> doScan();
}

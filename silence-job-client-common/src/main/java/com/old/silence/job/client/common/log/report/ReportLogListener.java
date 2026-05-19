package com.old.silence.job.client.common.log.report;

import com.old.silence.job.client.common.NettyClient;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.LogTaskDTO;
import com.old.silence.job.common.window.Listener;
import com.old.silence.job.log.SilenceJobLog;
import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * 批量异步上报
 *
 */

public class ReportLogListener implements Listener<LogTaskDTO> {

    private static final NettyClient CLIENT = RequestBuilder.<NettyClient, SilenceJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(nettyResult -> SilenceJobLog.LOCAL.info("Data report log successfully requestId:[{}]", nettyResult.getReqId())).build();

    @Override
    public void handler(List<LogTaskDTO> list) {
        // 调试日志：打印即将发送的 list 内容
        SilenceJobLog.LOCAL.info("=== Client Debug: ReportLogListener.handler() ===");
        SilenceJobLog.LOCAL.info("=== Client Debug: list class = [{}]", list.getClass().getName());
        SilenceJobLog.LOCAL.info("=== Client Debug: list size = [{}]", list.size());
        if (list != null && !list.isEmpty()) {
            SilenceJobLog.LOCAL.info("=== Client Debug: list content ===\n[{}]", JSON.toJSONString(list));
            Object first = list.get(0);
            SilenceJobLog.LOCAL.info("=== Client Debug: list.get(0) class = [{}]", first.getClass().getName());
            SilenceJobLog.LOCAL.info("=== Client Debug: list.get(0) value = [{}]", JSON.toJSONString(first));
        }

        CLIENT.reportLogTask(list);
    }
}

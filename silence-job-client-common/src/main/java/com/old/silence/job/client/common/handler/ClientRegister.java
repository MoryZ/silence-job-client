package com.old.silence.job.client.common.handler;

import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.NettyClient;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.common.constant.SystemConstants.BEAT;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class ClientRegister implements Lifecycle {
    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "sj-client-register"));
    public static final NettyClient CLIENT;
    public static final int REGISTER_TIME = 10;

    static {
        CLIENT = RequestBuilder.<NettyClient, SilenceJobRpcResult>newBuilder()
            .client(NettyClient.class)
            .callback(
                nettyResult -> {
                    if (500 == nettyResult.getCode()) {
                        SilenceJobLog.LOCAL.error("heartbeat check requestId:[{}] message:[{}]", nettyResult.getReqId(), nettyResult.getMessage());
                    }
                })
            .build();
    }

    @Override
    public void start() {
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                CLIENT.beat(BEAT.PING);
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("发送心跳失败", e);
            }
        }, 0, REGISTER_TIME, TimeUnit.SECONDS);
    }

    @Override
    public void close() {

    }
}

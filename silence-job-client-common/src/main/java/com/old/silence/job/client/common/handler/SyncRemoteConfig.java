package com.old.silence.job.client.common.handler;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.NettyClient;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class SyncRemoteConfig implements Lifecycle {
    private static final NettyClient CLIENT;
    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "sync-remote-config"));

    static {
        CLIENT = RequestBuilder.<NettyClient, SilenceJobRpcResult>newBuilder()
                .client(NettyClient.class)
                .timeout(1000L)
                .callback(nettyResult -> {
                    if (Objects.isNull(nettyResult.getData())) {
                        SilenceJobLog.LOCAL.debug("获取配置结果为null");
                        return;
                    }

                    GroupVersionCache.setConfig(
                            JSON.parseObject(nettyResult.getData().toString(), ConfigDTO.class));
                }).build();
    }

    @Override
    public void start() {
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                CLIENT.syncRemoteConfig();
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("通知配置失败", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void close() {
    }
}

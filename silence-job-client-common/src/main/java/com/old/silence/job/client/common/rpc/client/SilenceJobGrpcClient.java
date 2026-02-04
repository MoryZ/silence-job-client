package com.old.silence.job.client.common.rpc.client;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.event.SilenceChannelReconnectEvent;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.log.SilenceJobLog;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SilenceJobGrpcClient implements Lifecycle {
    private ManagedChannel channel;
    private final SilenceJobProperties silenceJobProperties;
    private static final ScheduledExecutorService SCHEDULE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "sj-client-check"));

    public SilenceJobGrpcClient(SilenceJobProperties silenceJobProperties) {
        this.silenceJobProperties = silenceJobProperties;
    }

    @Override
    public void start() {
        if (RpcType.GRPC != silenceJobProperties.getRpcType()) {
            return;
        }

        channel = connection();
        GrpcChannel.setChannel(channel);
        SilenceJobLog.LOCAL.info("grpc client started connect to server");

        // 连接检测
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(() -> {
            ConnectivityState state = channel.getState(true);
            if (state == ConnectivityState.TRANSIENT_FAILURE) {
                try {
                    // 抛出重连事件
                    SilenceSpringContext.getContext().publishEvent(new SilenceChannelReconnectEvent());
                } catch (Exception e) {
                    SilenceJobLog.LOCAL.error("reconnect error ", e);
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

    @Override
    public void close() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    public ManagedChannel connection() {
        SilenceJobProperties.RpcClientProperties clientRpc = silenceJobProperties.getClientRpc();
        // 创建 gRPC 频道
        String serverHost = GrpcChannel.getServerHost();
        return NettyChannelBuilder.forAddress(serverHost, GrpcChannel.getServerPort())
                .executor(createGrpcExecutor(serverHost))
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientRpc.getMaxInboundMessageSize())
                .keepAliveTime(clientRpc.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientRpc.getKeepAliveTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .usePlaintext().enableRetry().maxRetryAttempts(16)
                .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(String serverIp) {
        SilenceJobProperties.RpcClientProperties clientRpc = silenceJobProperties.getClientRpc();
        SilenceJobProperties.ThreadPoolConfig threadPool = clientRpc.getClientTp();
        serverIp = serverIp.replaceAll("%", "-");
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("silence-job-grpc-client-executor-" + serverIp + "-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }
}

package com.old.silence.job.client.common.rpc.server;

import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.grpc.util.MutableHandlerRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.common.rpc.client.GrpcChannel;
import com.old.silence.job.client.common.rpc.supports.handler.SilenceDispatcherRequestHandler;
import com.old.silence.job.client.common.rpc.supports.handler.UnaryRequestHandler;
import com.old.silence.job.common.constant.GrpcServerConstants;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.common.grpc.auto.GrpcResult;
import com.old.silence.job.common.grpc.auto.GrpcSilenceJobRequest;
import com.old.silence.job.log.SilenceJobLog;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Grpc server
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SilenceGrpcServer implements Lifecycle {
    private final SilenceJobProperties silenceJobProperties;
    private final SilenceDispatcherRequestHandler silenceDispatcherRequestHandler;
    private volatile boolean started = false;
    private Server server;

    public SilenceGrpcServer(SilenceJobProperties silenceJobProperties,
                             SilenceDispatcherRequestHandler silenceDispatcherRequestHandler) {
        this.silenceJobProperties = silenceJobProperties;
        this.silenceDispatcherRequestHandler = silenceDispatcherRequestHandler;
    }

    @Override
    public void start() {
        if (started || RpcType.GRPC != silenceJobProperties.getRpcType()) {
            return;
        }

        SilenceJobProperties.RpcServerProperties grpc = silenceJobProperties.getServerRpc();

        MutableHandlerRegistry handlerRegistry = new MutableHandlerRegistry();
        addServices(handlerRegistry, new GrpcInterceptor());
        NettyServerBuilder builder = NettyServerBuilder.forPort(GrpcChannel.getClientPort())
            .executor(createGrpcExecutor(grpc.getDispatcherTp()));

        Duration keepAliveTime = grpc.getKeepAliveTime();
        Duration keepAliveTimeOut = grpc.getKeepAliveTimeout();
        Duration permitKeepAliveTime = grpc.getPermitKeepAliveTime();

        server = builder.maxInboundMessageSize(grpc.getMaxInboundMessageSize()).fallbackHandlerRegistry(handlerRegistry)
            .compressorRegistry(CompressorRegistry.getDefaultInstance())
            .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
            .keepAliveTime(keepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .keepAliveTimeout(keepAliveTimeOut.toMillis(), TimeUnit.MILLISECONDS)
            .permitKeepAliveTime(permitKeepAliveTime.toMillis(), TimeUnit.MILLISECONDS)
            .build();
        try {
            server.start();
            this.started = true;
            SilenceJobLog.LOCAL.info("------> client silence-job remoting server start success, grpc = {}, port = {}",
                SilenceGrpcServer.class.getName(), silenceJobProperties.getPort());
        } catch (IOException e) {
            SilenceJobLog.LOCAL.error("--------> client silence-job remoting server error.", e);
            started = false;
            throw new SilenceJobClientException("client silence-job server start error");
        }
    }

    @Override
    public void close() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    private void addServices(MutableHandlerRegistry handlerRegistry, ServerInterceptor... serverInterceptor) {

        // 创建服务UNARY类型定义
        ServerServiceDefinition serviceDefinition = createUnaryServiceDefinition(
            GrpcServerConstants.UNARY_SERVICE_NAME, GrpcServerConstants.UNARY_METHOD_NAME,
            new UnaryRequestHandler(silenceJobProperties.getServerRpc().getDispatcherTp(), silenceDispatcherRequestHandler));
        handlerRegistry.addService(serviceDefinition);
        handlerRegistry.addService(ServerInterceptors.intercept(serviceDefinition, serverInterceptor));
    }

    public static ServerServiceDefinition createUnaryServiceDefinition(
        String serviceName,
        String methodName,
        ServerCalls.UnaryMethod<GrpcSilenceJobRequest, GrpcResult> unaryMethod) {

        MethodDescriptor<GrpcSilenceJobRequest, GrpcResult> methodDescriptor =
            MethodDescriptor.<GrpcSilenceJobRequest, GrpcResult>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(ProtoUtils.marshaller(GrpcSilenceJobRequest.getDefaultInstance()))
                .setResponseMarshaller(ProtoUtils.marshaller(GrpcResult.getDefaultInstance()))
                .build();

        return ServerServiceDefinition.builder(serviceName)
            .addMethod(methodDescriptor, ServerCalls.asyncUnaryCall(unaryMethod))
            .build();
    }

    private ThreadPoolExecutor createGrpcExecutor(SilenceJobProperties.ThreadPoolConfig threadPool) {
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("silence-job-grpc-server-executor-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }

    public SilenceJobProperties getSilenceJobProperties() {
        return silenceJobProperties;
    }

    public SilenceDispatcherRequestHandler getSilenceDispatcherRequestHandler() {
        return silenceDispatcherRequestHandler;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}

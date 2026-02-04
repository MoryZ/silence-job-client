package com.old.silence.job.client.common.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.event.SilenceServerStartFailedEvent;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.common.rpc.client.NettyChannel;
import com.old.silence.job.client.common.rpc.supports.handler.NettyHttpServerHandler;
import com.old.silence.job.client.common.rpc.supports.handler.SilenceDispatcherRequestHandler;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.RpcType;
import com.old.silence.job.log.SilenceJobLog;

/**
 * netty server
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SilenceNettyHttpServer implements Runnable, Lifecycle {
    private final SilenceJobProperties silenceJobProperties;
    private final SilenceDispatcherRequestHandler silenceDispatcherRequestHandler;
    private Thread thread = null;
    private volatile boolean started = false;

    public SilenceNettyHttpServer(SilenceJobProperties silenceJobProperties,
                                  SilenceDispatcherRequestHandler silenceDispatcherRequestHandler) {
        this.silenceJobProperties = silenceJobProperties;
        this.silenceDispatcherRequestHandler = silenceDispatcherRequestHandler;
    }

    @Override
    public void run() {
        // 防止重复启动
        if (started) {
            return;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // start server
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpServerHandler(silenceDispatcherRequestHandler, silenceJobProperties));
                        }
                    });

            // 在特定端口绑定并启动服务器 默认是1789
            ChannelFuture future = bootstrap.bind(NettyChannel.getClientPort()).sync();

            SilenceJobLog.LOCAL.info("------> silence-job client remoting server start success, nettype = {}, port = {}",
                    SilenceNettyHttpServer.class.getName(), silenceJobProperties.getPort());

            started = true;
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            SilenceJobLog.LOCAL.info("--------> silence-job client remoting server stop.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("--------> silence-job client remoting server error.", e);
            started = false;
            // Silence Netty Server 未启动
            SilenceSpringContext.getContext().publishEvent(new SilenceServerStartFailedEvent());
            throw new SilenceJobClientException("silence-job client server start error");
        } finally {
            // 当服务器正常关闭时，关闭EventLoopGroups以释放资源。
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void start() {
        if (RpcType.NETTY != silenceJobProperties.getRpcType()) {
            return;
        }
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void close() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}

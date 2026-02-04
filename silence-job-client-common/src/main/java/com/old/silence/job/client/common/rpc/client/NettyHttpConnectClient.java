package com.old.silence.job.client.common.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.handler.ClientRegister;
import com.old.silence.job.log.SilenceJobLog;

import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Netty 客户端
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NettyHttpConnectClient implements Lifecycle {
    private final SilenceJobProperties silenceJobProperties;
    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = new Bootstrap();
    private Channel channel;

    public NettyHttpConnectClient(SilenceJobProperties silenceJobProperties) {
        this.silenceJobProperties = silenceJobProperties;
    }

    @Override
    public void start() {

        try {
            NettyHttpConnectClient thisClient = this;
            bootstrap.group(nioEventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(NettyChannel.getServerHost(), NettyChannel.getServerPort())
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new IdleStateHandler(0, 0, 3 * ClientRegister.REGISTER_TIME, TimeUnit.SECONDS))
                                    .addLast(new HttpClientCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new NettyHttpClientHandler(thisClient));
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

            // 开启连接服务端
            connect();
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("Client start exception", e);
        }
    }

    /**
     * 连接客户端
     *
     */
    public void connect() {

        try {
            ChannelFuture channelFuture = bootstrap.connect();

            boolean notTimeout = channelFuture.awaitUninterruptibly(30, TimeUnit.SECONDS);
            channel = channelFuture.channel();
            if (notTimeout) {
                // 连接成功
                if (channel != null && channel.isActive()) {
                    SilenceJobLog.LOCAL.info("netty client started {} connect to server", channel.localAddress());
                    NettyChannel.setChannel(getChannel());
                    return;
                }

                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    exceptionHandler(cause);
                }
            } else {
                SilenceJobLog.LOCAL.warn("connect remote host[{}] timeout {}s", channel.remoteAddress(), 30);
            }
        } catch (Exception e) {
            exceptionHandler(e);
        }

        // 若连接失败尝试关闭改channel
        if (Objects.nonNull(channel)) {
            channel.close();
        }
    }

    /**
     * 重连
     */
    public void reconnect() {
        ChannelFuture channelFuture = bootstrap
                .remoteAddress(NettyChannel.getServerHost(), NettyChannel.getServerPort())
                .connect();
        channelFuture.addListener((ChannelFutureListener) future -> {
            Throwable cause = future.cause();
            if (cause != null) {
                exceptionHandler(cause);
            } else {
                channel = channelFuture.channel();
                if (channel != null && channel.isActive()) {
                    SilenceJobLog.LOCAL.info("Netty client {} reconnect to server", channel.localAddress());
                    NettyChannel.setChannel(getChannel());
                }
            }
        });
    }

    /**
     * 连接失败处理
     *
     * @param cause d
     */
    private void exceptionHandler(Throwable cause) {
        if (cause instanceof ConnectException) {
            SilenceJobLog.LOCAL.error("connect error:{}", cause.getMessage());
        } else if (cause instanceof ClosedChannelException) {
            SilenceJobLog.LOCAL.error("connect error:{}", "client has destroy");
        } else {
            SilenceJobLog.LOCAL.error("connect error:", cause);
        }
    }


    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
        nioEventLoopGroup.shutdownGracefully();
    }

    public SilenceJobProperties getSilenceJobProperties() {
        return silenceJobProperties;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}

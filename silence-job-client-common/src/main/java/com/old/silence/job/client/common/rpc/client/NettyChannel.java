package com.old.silence.job.client.common.rpc.client;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.exception.SilenceJobRemoteException;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.HeadersEnum;
import com.old.silence.job.common.util.NetUtil;
import com.old.silence.job.log.SilenceJobLog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;



public class NettyChannel {
    private NettyChannel() {
    }

    private static Channel CHANNEL;

    /**
     * 服务端端口
     */
    private static final String SILENCE_JOB_SERVER_PORT = "silence-job.server.port";
    /**
     * 服务端host
     */
    private static final String SILENCE_JOB_SERVER_HOST = "silence-job.server.host";

    /**
     * 客户端端口
     */
    private static final String SILENCE_JOB_CLIENT_PORT = "silence-job.port";
    /**
     * 客户端host
     */
    private static final String SILENCE_JOB_CLIENT_HOST = "silence-job.host";

    private static final Integer MIN_PORT = 15000;
    private static final Integer MAX_PORT = 50000;
    private static final ReentrantLock PORT_LOCK = new ReentrantLock();
    private static final Integer RANDOM_CLIENT_PORT = -1;

    private static final String HOST_ID = IdUtil.getSnowflake().nextIdStr();
    private static final int PORT;
    private static final String HOST;

    static {
        PORT = Integer.parseInt(System.getProperty(SILENCE_JOB_CLIENT_PORT, String.valueOf(1789)));
        HOST = System.getProperty(SILENCE_JOB_CLIENT_HOST, NetUtil.getLocalIpStr());
    }

    /**
     * 获取服务端端口
     *
     * @return port
     */
    public static int getServerPort() {
        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getContext().getBean(SilenceJobProperties.class);
        SilenceJobProperties.ServerConfig serverConfig = silenceJobProperties.getServer();

        String port = System.getProperty(SILENCE_JOB_SERVER_PORT);
        if (StrUtil.isBlank(port)) {
            System.setProperty(SILENCE_JOB_SERVER_PORT, String.valueOf(serverConfig.getPort()));
        }

        return Integer.parseInt(System.getProperty(SILENCE_JOB_SERVER_PORT));
    }

    /**
     * 获取服务端host
     *
     * @return host
     */
    public static String getServerHost() {
        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);
        SilenceJobProperties.ServerConfig serverConfig = silenceJobProperties.getServer();

        String host = System.getProperty(SILENCE_JOB_SERVER_HOST);
        if (StrUtil.isBlank(host)) {
            System.setProperty(SILENCE_JOB_SERVER_HOST, serverConfig.getHost());
        }

        return System.getProperty(SILENCE_JOB_SERVER_HOST);
    }

    /**
     * 获取指定的客户IP
     *
     * @return 客户端IP
     */
    public static String getClientHost() {
        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);

        String host = silenceJobProperties.getHost();
        // 获取客户端指定的IP地址
        if (StrUtil.isBlank(host)) {
            host = HOST;
        }

        return host;
    }

    /**
     * 获取客户端端口
     *
     * @return port 端口
     */
    public static Integer getClientPort() {
        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);
        ServerProperties serverProperties = SilenceSpringContext.getBean(ServerProperties.class);

        Integer port = silenceJobProperties.getPort();
        // 获取客户端指定的端口
        if (Objects.isNull(port)) {
            port = Optional.ofNullable(serverProperties.getPort()).orElse(PORT);
            silenceJobProperties.setPort(port);
            SilenceJobLog.LOCAL.info("silence job client port :{}", port);
        } else if (port.equals(RANDOM_CLIENT_PORT)) {
            // 使用随机算法获取端口
            PORT_LOCK.lock();
            try {
                // 双重检查，避免重复获取端口
                if (silenceJobProperties.getPort().equals(RANDOM_CLIENT_PORT)) {
                    port = getAvailablePort();
                    silenceJobProperties.setPort(port);
                    SilenceJobLog.LOCAL.info("silence job client port :{}", port);
                } else {
                    port = silenceJobProperties.getPort();
                }
            } finally {
                PORT_LOCK.unlock();
            }
        }

        return port;
    }

    /**
     * 获取随机可用的端口
     *
     * @return 可用端口号
     */
    private static Integer getAvailablePort() {
        int port;
        do {
            port = MIN_PORT + (int) (Math.random() * (MAX_PORT - MIN_PORT));
        } while (!isPortAvailable(port));

        return port;
    }

    /**
     * 检查端口是否可以使用
     *
     * @param port 端口号
     * @return 是否可用
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            // 设置端口重用
            serverSocket.setReuseAddress(true);
            // 绑定端口
            serverSocket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public static void setChannel(Channel channel) {
        NettyChannel.CHANNEL = channel;
    }

    /**
     * 发送数据
     *
     * @param method 请求方式
     * @param url    url地址
     * @param body   请求的消息体
     * @throws InterruptedException
     */
    public static void send(HttpMethod method, String url, String body) throws InterruptedException {

        if (Objects.isNull(CHANNEL)) {
            SilenceJobLog.LOCAL.error("send message but channel is null url:[{}] method:[{}] body:[{}] ", url, method, body);
            return;
        }

        // 配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, method, url, Unpooled.wrappedBuffer(body.getBytes(StandardCharsets.UTF_8)));

        SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);

        // server配置不能为空
        SilenceJobProperties.ServerConfig serverConfig = silenceJobProperties.getServer();
        if (Objects.isNull(serverConfig)) {
            SilenceJobLog.LOCAL.error("silence job server config is null");
            return;
        }

        Assert.notBlank(silenceJobProperties.getGroup(),
                () -> new SilenceJobRemoteException("The group is null, please check if your configuration is correct."));

        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
                .set(HeadersEnum.HOST_ID.getKey(), HOST_ID)
                .set(HeadersEnum.HOST_IP.getKey(), getClientHost())
                .set(HeadersEnum.GROUP_NAME.getKey(), silenceJobProperties.getGroup())
                .set(HeadersEnum.HOST_PORT.getKey(), getClientPort())
                .set(HeadersEnum.VERSION.getKey(), GroupVersionCache.getVersion())
                .set(HeadersEnum.HOST.getKey(), serverConfig.getHost())
                .set(HeadersEnum.NAMESPACE.getKey(), Optional.ofNullable(silenceJobProperties.getNamespace()).orElse(
                        SystemConstants.DEFAULT_NAMESPACE))
                .set(HeadersEnum.TOKEN.getKey(), Optional.ofNullable(silenceJobProperties.getToken()).orElse(
                        SystemConstants.DEFAULT_TOKEN))
        ;

        //发送数据
        try {
            CHANNEL.writeAndFlush(request).sync();
        } catch (Exception exception) {
            throw new SilenceJobRemoteException("网络异常");
        }
    }
}

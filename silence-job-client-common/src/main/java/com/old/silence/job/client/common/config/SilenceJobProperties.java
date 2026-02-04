package com.old.silence.job.client.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import com.old.silence.job.common.alarm.email.SilenceJobMailProperties;
import com.old.silence.job.common.enums.RpcType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * silence job 客户端配置
 *
 */
@Configuration
@ConfigurationProperties(prefix = "silence-job")
public class SilenceJobProperties {

    /**
     * 命名空间ID
     * 若不填则默认为 SystemConstants::DEFAULT_NAMESPACE
     */
    private String namespace;

    /**
     * 服务端对应的group
     */
    private String group;

    /**
     * 令牌
     * 若不填则默认为 SystemConstants::DEFAULT_TOKEN
     */
    private String token;

    /**
     * 指定客户端IP，默认取本地IP
     */
    private String host;

    /**
     * 指定客户端端口
     */
    private Integer port = 17889;

    /**
     * rpc类型
     */
    private RpcType rpcType = RpcType.GRPC;

    /**
     * 重试、调度日志远程上报滑动窗口配置
     */
    private LogSlidingWindowConfig logSlidingWindow = new LogSlidingWindowConfig();

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 重试模块配置
     */
    private Retry retry = new Retry();

    /**
     * 邮件配置
     */
    @NestedConfigurationProperty
    private SilenceJobMailProperties mail = new SilenceJobMailProperties();

    /**
     * 客户端脚本存储位置
     */
    private String workspace;

    /**
     * 客户端Rpc配置
     */
    private RpcClientProperties clientRpc = new RpcClientProperties();

    /**
     * 服务端Rpc配置
     */
    private RpcServerProperties serverRpc = new RpcServerProperties();

    public static class ServerConfig {
        /**
         * 服务端的地址，若服务端集群部署则此处配置域名
         */
        private String host = "127.0.0.1";

        /**
         * 服务端netty的端口号
         */
        private int port = 17888;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class SlidingWindowConfig {

        /**
         * 总量窗口期阈值
         */
        private int totalThreshold = 50;

        /**
         * 窗口数量预警
         */
        private int windowTotalThreshold = 150;

        /**
         * 窗口期时间长度
         */
        private long duration = 10;

        /**
         * 窗口期单位
         */
        private ChronoUnit chronoUnit = ChronoUnit.SECONDS;

        public int getTotalThreshold() {
            return totalThreshold;
        }

        public void setTotalThreshold(int totalThreshold) {
            this.totalThreshold = totalThreshold;
        }

        public int getWindowTotalThreshold() {
            return windowTotalThreshold;
        }

        public void setWindowTotalThreshold(int windowTotalThreshold) {
            this.windowTotalThreshold = windowTotalThreshold;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public ChronoUnit getChronoUnit() {
            return chronoUnit;
        }

        public void setChronoUnit(ChronoUnit chronoUnit) {
            this.chronoUnit = chronoUnit;
        }
    }

    public static class LogSlidingWindowConfig {

        /**
         * 总量窗口期阈值
         */
        private int totalThreshold = 50;

        /**
         * 窗口数量预警
         */
        private int windowTotalThreshold = 150;

        /**
         * 窗口期时间长度
         */
        private long duration = 5;

        /**
         * 窗口期单位
         */
        private ChronoUnit chronoUnit = ChronoUnit.SECONDS;

        public int getTotalThreshold() {
            return totalThreshold;
        }

        public void setTotalThreshold(int totalThreshold) {
            this.totalThreshold = totalThreshold;
        }

        public int getWindowTotalThreshold() {
            return windowTotalThreshold;
        }

        public void setWindowTotalThreshold(int windowTotalThreshold) {
            this.windowTotalThreshold = windowTotalThreshold;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public ChronoUnit getChronoUnit() {
            return chronoUnit;
        }

        public void setChronoUnit(ChronoUnit chronoUnit) {
            this.chronoUnit = chronoUnit;
        }
    }

    public static class Retry {
        /**
         * 远程上报滑动窗口配置
         */
        private SlidingWindowConfig reportSlidingWindow = new SlidingWindowConfig();

        /**
         * 本地执行重试或者回调配置
         */
        private ThreadPoolConfig dispatcherThreadPool = new ThreadPoolConfig(32, 32, 1, TimeUnit.SECONDS , 10000);

        public SlidingWindowConfig getReportSlidingWindow() {
            return reportSlidingWindow;
        }

        public void setReportSlidingWindow(SlidingWindowConfig reportSlidingWindow) {
            this.reportSlidingWindow = reportSlidingWindow;
        }

        public ThreadPoolConfig getDispatcherThreadPool() {
            return dispatcherThreadPool;
        }

        public void setDispatcherThreadPool(ThreadPoolConfig dispatcherThreadPool) {
            this.dispatcherThreadPool = dispatcherThreadPool;
        }
    }

    public static class RpcServerProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(2, ChronoUnit.HOURS);

        private Duration keepAliveTimeout = Duration.of(20, ChronoUnit.SECONDS);

        private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig dispatcherTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS , 10000);

        public int getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public void setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public Duration getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Duration keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Duration getKeepAliveTimeout() {
            return keepAliveTimeout;
        }

        public void setKeepAliveTimeout(Duration keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public Duration getPermitKeepAliveTime() {
            return permitKeepAliveTime;
        }

        public void setPermitKeepAliveTime(Duration permitKeepAliveTime) {
            this.permitKeepAliveTime = permitKeepAliveTime;
        }

        public ThreadPoolConfig getDispatcherTp() {
            return dispatcherTp;
        }

        public void setDispatcherTp(ThreadPoolConfig dispatcherTp) {
            this.dispatcherTp = dispatcherTp;
        }
    }

    public static class RpcClientProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(2, ChronoUnit.HOURS);

        private Duration keepAliveTimeout = Duration.of(20, ChronoUnit.SECONDS);

        private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig clientTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS , 10000);

        public int getMaxInboundMessageSize() {
            return maxInboundMessageSize;
        }

        public void setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public Duration getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Duration keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Duration getKeepAliveTimeout() {
            return keepAliveTimeout;
        }

        public void setKeepAliveTimeout(Duration keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public Duration getPermitKeepAliveTime() {
            return permitKeepAliveTime;
        }

        public void setPermitKeepAliveTime(Duration permitKeepAliveTime) {
            this.permitKeepAliveTime = permitKeepAliveTime;
        }

        public ThreadPoolConfig getClientTp() {
            return clientTp;
        }

        public void setClientTp(ThreadPoolConfig clientTp) {
            this.clientTp = clientTp;
        }
    }


    public static class ThreadPoolConfig {

        /**
         * 核心线程池
         */
        private int corePoolSize = 16;

        /**
         * 最大线程数
         */
        private int maximumPoolSize = 16;

        /**
         * 线程存活时间
         */
        private long keepAliveTime = 1;

        /**
         * 线程存活时间(单位)
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 队列容量
         */
        private int queueCapacity = 10000;

        public ThreadPoolConfig() {
        }

        public ThreadPoolConfig(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, int queueCapacity) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
            this.timeUnit = timeUnit;
            this.queueCapacity = queueCapacity;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public void setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public RpcType getRpcType() {
        return rpcType;
    }

    public void setRpcType(RpcType rpcType) {
        this.rpcType = rpcType;
    }

    public LogSlidingWindowConfig getLogSlidingWindow() {
        return logSlidingWindow;
    }

    public void setLogSlidingWindow(LogSlidingWindowConfig logSlidingWindow) {
        this.logSlidingWindow = logSlidingWindow;
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public SilenceJobMailProperties getMail() {
        return mail;
    }

    public void setMail(SilenceJobMailProperties mail) {
        this.mail = mail;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public RpcClientProperties getClientRpc() {
        return clientRpc;
    }

    public void setClientRpc(RpcClientProperties clientRpc) {
        this.clientRpc = clientRpc;
    }

    public RpcServerProperties getServerRpc() {
        return serverRpc;
    }

    public void setServerRpc(RpcServerProperties serverRpc) {
        this.serverRpc = serverRpc;
    }
}

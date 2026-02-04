package com.old.silence.job.client.common.rpc.supports.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.rpc.supports.http.HttpRequest;
import com.old.silence.job.client.common.rpc.supports.http.HttpResponse;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final ThreadPoolExecutor dispatcherThreadPool;
    private final SilenceDispatcherRequestHandler dispatcher;

    public NettyHttpServerHandler(SilenceDispatcherRequestHandler silenceDispatcherRequestHandler,
                                  SilenceJobProperties silenceJobProperties) {
        this.dispatcher = silenceDispatcherRequestHandler;

        // 获取线程池配置
        SilenceJobProperties.RpcServerProperties rpcServerProperties = silenceJobProperties.getServerRpc();
        SilenceJobProperties.ThreadPoolConfig threadPool = rpcServerProperties.getDispatcherTp();
        dispatcherThreadPool = new ThreadPoolExecutor(
                threadPool.getCorePoolSize(), threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(),
                threadPool.getTimeUnit(), new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
                new CustomizableThreadFactory("silence-netty-server-"));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) {

        String content = fullHttpRequest.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = fullHttpRequest.headers();
        Map<String, String> headerMap = new HashMap<>();

        for (Entry<String, String> header : headers) {
            headerMap.put(header.getKey(), header.getValue());
        }

        String uri = fullHttpRequest.uri();
        NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(channelHandlerContext, content, HttpUtil.isKeepAlive(fullHttpRequest),
                fullHttpRequest.method(), uri, headers, new HttpResponse(), new HttpRequest(headerMap, uri));

        // 执行任务
        dispatcherThreadPool.execute(() -> {
            SilenceJobRpcResult silenceJobRpcResult = null;
            try {
                silenceJobRpcResult = dispatcher.dispatch(nettyHttpRequest);
            } catch (Exception e) {
                SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
                silenceJobRpcResult = new SilenceJobRpcResult(500, e.getMessage(), null, retryRequest.getReqId());
            } finally {
                writeResponse(channelHandlerContext,
                        HttpUtil.isKeepAlive(fullHttpRequest),
                        nettyHttpRequest.getHttpResponse(),
                        JSON.toJSONString(silenceJobRpcResult)
                );
            }
        });
    }

    private void writeResponse(ChannelHandlerContext ctx, boolean keepAlive, final HttpResponse httpResponse,
                               String responseJson) {
        // write response
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,
                HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        Map<String, Object> headers = httpResponse.getHeaders();
        headers.forEach((key, value) -> response.headers().set(key, value));
        ctx.writeAndFlush(response);
    }

}

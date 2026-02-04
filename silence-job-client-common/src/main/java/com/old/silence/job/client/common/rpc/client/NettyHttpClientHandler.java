package com.old.silence.job.client.common.rpc.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.event.SilenceChannelReconnectEvent;
import com.old.silence.job.client.common.handler.ClientRegister;
import com.old.silence.job.common.constant.SystemConstants.BEAT;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.rpc.RpcContext;
import com.old.silence.job.log.SilenceJobLog;

import java.util.concurrent.TimeUnit;

/**
 * netty 客户端处理器
 *
 */

public class NettyHttpClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final NettyHttpConnectClient nettyHttpConnectClient;

    public NettyHttpClientHandler(NettyHttpConnectClient nettyHttpConnectClient) {
        this.nettyHttpConnectClient = nettyHttpConnectClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {

        String content = msg.content().toString(CharsetUtil.UTF_8);
        HttpHeaders headers = msg.headers();

        SilenceJobLog.LOCAL.debug("Receive server data content:[{}], headers:[{}]", content, headers);
        SilenceJobRpcResult silenceJobRpcResult = JSON.parseObject(content, SilenceJobRpcResult.class);
        RpcContext.invoke(silenceJobRpcResult.getReqId(), silenceJobRpcResult, false);

    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        SilenceJobLog.LOCAL.debug("channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SilenceJobLog.LOCAL.debug("channelUnregistered");
        ctx.channel().eventLoop().schedule(() -> {
            try {
                // 抛出重连事件
                SilenceSpringContext.getContext().publishEvent(new SilenceChannelReconnectEvent());
                nettyHttpConnectClient.reconnect();
            } catch (Exception e) {
                SilenceJobLog.LOCAL.error("reconnect error ", e);
            }

        }, 10, TimeUnit.SECONDS);


    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        SilenceJobLog.LOCAL.debug("channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SilenceJobLog.LOCAL.debug("channelInactive");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        SilenceJobLog.LOCAL.debug("channelReadComplete");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
        SilenceJobLog.LOCAL.debug("channelWritabilityChanged");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SilenceJobLog.LOCAL.error("silence-job netty-http client exception", cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        SilenceJobLog.LOCAL.debug("userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            ClientRegister.CLIENT.beat(BEAT.PING);
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}

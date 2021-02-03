package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.enums.RpcMessageType;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 * @author SiMan
 * @date 2021/1/20 16:04
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
//    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}]", rpcResponse.toString());
            unprocessedRequests.complete(rpcResponse);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());
                RpcRequest rpcRequest = RpcRequest.builder().rpcMessageType(RpcMessageType.HEART_BEAT).build();
                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.model.RpcMessage;
import com.siman.qrpc.model.RpcResponse;
import com.siman.qrpc.remoting.constans.RpcConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义客户端 ChannelHandler 来处理服务端发过来的数据
 * @author SiMan
 * @date 2021/1/20 16:04
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
//    private final UnprocessedRequests unprocessedRequests;
//    private final NettyRpcClient nettyRpcClient;
//
//    public NettyRpcClientHandler() {
//        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
//    }

    /**
     * 读取服务端传输的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RpcResponse rpcResponse = (RpcResponse) msg;
            log.info("client receive msg: [{}]", rpcResponse.toString());
            // 声明一个 AttributeKey 对象
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcResponse.getRequestId());
            // 将服务端的返回结果保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源
            // AttributeMap的key是AttributeKey，value是Attribute
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 处理客户端消息发生异常的时候被调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

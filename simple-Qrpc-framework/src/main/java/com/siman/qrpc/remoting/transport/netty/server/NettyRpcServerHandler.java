package com.siman.qrpc.remoting.transport.netty.server;

import com.siman.qrpc.enums.RpcResponseCodeEnum;
import com.siman.qrpc.enums.SerializationTypeEnum;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.handler.RpcRequestHandler;
import com.siman.qrpc.model.RpcMessage;
import com.siman.qrpc.model.RpcRequest;
import com.siman.qrpc.model.RpcResponse;
import com.siman.qrpc.util.threadpool.ThreadPoolFactoryUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 * @author SiMan
 * @date 2021/1/20 2:09
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;

    public NettyRpcServerHandler() {
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("netty-server-handler-rpc-pool");
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        try {
//            if (msg instanceof RpcMessage) {
//                log.info("server receive msg: [{}] ", msg);
//                byte messageType = ((RpcMessage) msg).getMessageType();
//                RpcMessage rpcMessage = new RpcMessage();
//                //TODO 设置序列化方式
////                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
//                //TODO 设置压缩方式
////                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
//                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
//                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
//                    rpcMessage.setData(RpcConstants.PONG);
//                } else {
//                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
//                    // 执行目标方法并返回结果
//                    Object result = rpcRequestHandler.handle(rpcRequest);
//                    log.info(String.format("server get result: %s", result.toString()));
//                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
//                    // 通道活跃且可写
//                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
//                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
//                        rpcMessage.setData(rpcResponse);
//                    } else {
//                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
//                        rpcMessage.setData(rpcResponse);
//                        log.error("not writable now, message dropped");
//                    }
//                }
//                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//            }
//        } finally {
//            // 是否 ByteBuf，防止内存泄漏
//            ReferenceCountUtil.release(msg);
//        }
//    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        threadPool.execute(() -> {
            try {
                RpcRequest rpcRequest = (RpcRequest) msg;
                log.info(String.format("server receive msg: %s", rpcRequest));
                //执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                // TODO 勿将result作为响应发送到客户端，导致 codec 与 clientHandler 不起作用
                ChannelFuture channelFuture = ctx.writeAndFlush(RpcResponse.success(result, rpcRequest.getRequestId()));
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            } finally {
                //确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}

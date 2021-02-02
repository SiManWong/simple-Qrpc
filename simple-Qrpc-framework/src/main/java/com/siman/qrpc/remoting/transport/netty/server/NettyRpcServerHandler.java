package com.siman.qrpc.remoting.transport.netty.server;

import com.siman.qrpc.enums.RpcMessageTypeEnum;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.remoting.handler.RpcRequestHandler;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 自定义服务端的 ChannelHandler 来处理客户端发过来的数据
 * @author SiMan
 * @date 2021/1/20 2:09
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
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


    /**
     * 读取从客户端消息，然后调用目标服务的目标方法并返回给客户端。
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            RpcRequest rpcRequest = (RpcRequest) msg;
            log.info(String.format("server receive msg: %s", rpcRequest));
            if (rpcRequest.getRpcMessageTypeEnum() == RpcMessageTypeEnum.HEART_BEAT) {
                log.info("receive heat beat msg from client");
                return;
            }
            //执行目标方法（客户端需要执行的方法）并且返回方法结果
            Object result = rpcRequestHandler.handle(rpcRequest);
            log.info(String.format("server get result: %s", result.toString()));
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                // 将 result 封装为 RpcResponse 响应给客户端
                RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                log.error("not writable now, message dropped");
            }
        } finally {
            //确保 ByteBuf 被释放，不然可能会有内存泄露问题
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the conneciton");
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

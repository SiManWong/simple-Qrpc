package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.model.RpcRequest;
import com.siman.qrpc.model.RpcResponse;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.codec.kryo.RpcDecoder;
import com.siman.qrpc.remoting.transport.netty.codec.kryo.RpcEncoder;
import com.siman.qrpc.serialize.impl.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用final关键字，JVM会对方法、变量及类进行优化。
 * 可以在多线程环境下安全的共享，不用额外的同步开销
 * @author SiMan
 * @date 2021/1/20 15:48
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {
//    private final ServiceDiscovery serviceDiscovery;
//    private final UnprocessedRequests unprocessedRequests;
    private final String host;
    private final int port;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 设置超时时间
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 15秒钟内没有数据发送到服务器，则发送心跳请求
//                        pipeline.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        // 解码器
                        pipeline.addLast("decode", new RpcDecoder(kryoSerializer, RpcResponse.class));
                        // 编码器
                        pipeline.addLast("encode", new RpcEncoder(kryoSerializer, RpcRequest.class));
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
//        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    /**
     * 发送消息到服务端
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            log.info("client connect {}", host + ":" + port);
            Channel channel = channelFuture.channel();
            if (channel != null) {
                channel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("client send message: [{}],", rpcRequest.toString());
                    } else {
                        log.error("Send failed:", future.cause());
                    }
                });
                // 阻塞等待，直到 channel 关闭
                channel.closeFuture().sync();
                // 将服务端返回的数据也就是 RpcResponse 对象取出
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
                RpcResponse rpcResponse = channel.attr(key).get();

                return rpcResponse;
            }

            return null;
        } catch (InterruptedException e) {
            log.error("occur exception when connect server:", e);
        }

        return null;
    }
}

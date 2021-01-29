package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.registry.zk.ZkServiceDiscover;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.remoting.transport.RpcMessageChecker;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.codec.RpcDecoder;
import com.siman.qrpc.remoting.transport.netty.codec.RpcEncoder;
import com.siman.qrpc.serialize.impl.KryoSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

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
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final ServiceDiscover serviceDiscover;

    public NettyRpcClient() {
        serviceDiscover = SingletonFactory.getInstance(ZkServiceDiscover.class);
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                // 是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 设置超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
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
            InetSocketAddress inetSocketAddress = serviceDiscover.lookupService(rpcRequest.getInterfaceName());
            ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress).sync();
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
                // 校验 RpcRequest 和 RpcResponse
                RpcMessageChecker.check(rpcResponse, rpcRequest);

                return rpcResponse;
            }

            return null;
        } catch (InterruptedException e) {
            log.error("occur exception when connect server:", e);
        }

        return null;
    }
}

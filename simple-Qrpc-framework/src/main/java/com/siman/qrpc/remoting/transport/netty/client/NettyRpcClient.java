package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.registry.zk.ZkServiceDiscover;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.remoting.model.RpcMessageChecker;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * 使用final关键字，JVM会对方法、变量及类进行优化。
 * 可以在多线程环境下安全的共享，不用额外的同步开销
 *
 * @author SiMan
 * @date 2021/1/20 15:48
 */
@Slf4j
public final class NettyRpcClient implements RpcRequestTransport {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final ServiceDiscover serviceDiscover;
    private final UnprocessedRequests unprocessedRequests;

    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        // bootstrap 引导 Netty 配置
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
                        // 解码器
                        pipeline.addLast("decode", new RpcDecoder(kryoSerializer, RpcResponse.class));
                        // 编码器
                        pipeline.addLast("encode", new RpcEncoder(kryoSerializer, RpcRequest.class));
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });

        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        serviceDiscover = SingletonFactory.getInstance(ZkServiceDiscover.class);
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功！");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // 构建返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // 从注册中心获取服务地址
        InetSocketAddress inetSocketAddress = serviceDiscover.lookupService(rpcRequest.getInterfaceName());
        Channel channel = ChannelProvider.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }
}

package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.enums.CompressTypeEnum;
import com.siman.qrpc.enums.SerializationTypeEnum;
import com.siman.qrpc.extension.ExtensionLoader;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.remoting.constans.RpcConstants;
import com.siman.qrpc.remoting.model.RpcMessage;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.siman.qrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    private final ChannelProvider channelProvider;
    private static final int MAX_RETRY = 5;


    public NettyRpcClient() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        // bootstrap 引导 Netty 配置
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 设置超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 5 秒内没有发送数据给服务端的话，就发送一次心跳请求
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        // 解码器
                        pipeline.addLast("decode", new RpcMessageDecoder());
                        // 编码器
                        pipeline.addLast("encode", new RpcMessageEncoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });

        unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        serviceDiscover = ExtensionLoader.getExtensionLoader(ServiceDiscover.class).getExtension("zk");
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress, int retry) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener( future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功！");
                // 告知任务完成，并将 channelFuture 作为结果返回
                completableFuture.complete(((ChannelFuture) future).channel());
            } else if (MAX_RETRY == 0) {
                throw new IllegalStateException();
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连间隔
                int delay = 1 << order;
                log.info("连接[{}]失败， 第 {} 次重连...", inetSocketAddress.toString(), order);
                bootstrap.config().group().schedule(()-> doConnect(inetSocketAddress, retry - 1), delay, TimeUnit.SECONDS);
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
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {
        // 构建返回值
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // build rpc service name by ppcRequest
        String serviceName = rpcRequest.toRpcProperties().getServiceName();
        // 从注册中心获取服务地址
        InetSocketAddress inetSocketAddress = serviceDiscover.lookupService(serviceName);
        Channel channel = getChannel(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);
            rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            rpcMessage.setData(rpcRequest);
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
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

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress, MAX_RETRY);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }
}

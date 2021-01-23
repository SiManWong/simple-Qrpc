package com.siman.qrpc.remoting.transport.netty.server;

import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.model.RpcRequest;
import com.siman.qrpc.model.RpcResponse;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.remoting.transport.netty.codec.kryo.RpcDecoder;
import com.siman.qrpc.remoting.transport.netty.codec.kryo.RpcEncoder;
import com.siman.qrpc.serialize.impl.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * 服务端
 * 接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 * @author SiMan
 * @date 2021/1/20 1:22
 */
@Slf4j
public class NettyRpcServer {
    public static final int PORT = 4396;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);

    public void registerService(Object service) {
        // 发布服务
        serviceProvider.publishService(service);
    }

    @SneakyThrows
    public void start() {
        // 服务器关闭后，注销所有服务
//        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        String localHost = "127.0.0.1";

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 耗时任务线程组
//        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
//                // cpu 核数 * 2
//                Runtime.getRuntime().availableProcessors() * 2,
//                ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
//        );

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            KryoSerializer kryoSerializer = new KryoSerializer();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    // 是否开启 Nagle 算法
//                    .childOption(ChannelOption.TCP_NODELAY, true)
//                    // 是否开启 TCP 底层心跳机制
//                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
//                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 30 秒之内没有收到客户端请求就关闭连接
//                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            // 解码器
                            pipeline.addLast("decode", new RpcDecoder(kryoSerializer, RpcRequest.class));
                            // 编码器
                            pipeline.addLast("encode", new RpcEncoder(kryoSerializer, RpcResponse.class));
                            // 请求处理器
                            pipeline.addLast(new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口
            ChannelFuture future = bootstrap.bind(localHost, PORT).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
//            serviceHandlerGroup.shutdownGracefully();
        }
    }
}

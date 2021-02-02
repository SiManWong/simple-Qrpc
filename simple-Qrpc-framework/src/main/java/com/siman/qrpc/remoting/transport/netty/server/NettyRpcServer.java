package com.siman.qrpc.remoting.transport.netty.server;

import com.siman.qrpc.annotation.RpcService;
import com.siman.qrpc.config.CustomShutdownHook;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.registry.ServiceRegistry;
import com.siman.qrpc.registry.zk.ZkServiceRegistry;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.remoting.transport.netty.codec.RpcDecoder;
import com.siman.qrpc.remoting.transport.netty.codec.RpcEncoder;
import com.siman.qrpc.serialize.impl.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 服务端
 * 接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 * @author SiMan
 * @date 2021/1/20 1:22
 */
@Slf4j
@Component
@PropertySource("classpath:rpc.properties")
public class NettyRpcServer implements InitializingBean, ApplicationContextAware {
    @Value("${rpc.server.host}")
    private String host;
    @Value("${rpc.server.port}")
    private int port;
    private final static ServiceRegistry serviceRegistry;
    private final static ServiceProvider serviceProvider;

    static {
        serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public void registerService(Object service) {
        // 发布服务
        serviceProvider.addService(service);
        Class<?> anInterface = service.getClass().getInterfaces()[0];
        serviceRegistry.registerService(anInterface.getCanonicalName(),new InetSocketAddress("127.0.0.1", port));
    }

    @SneakyThrows
    public void start() {
        // 服务器关闭后，注销所有服务
//        CustomShutdownHook.getCustomShutdownHook().clearAll();

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
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 30 秒之内没有收到客户端的请求就关闭连接
                            pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            // 解码器
                            pipeline.addLast("decode", new RpcDecoder(kryoSerializer, RpcRequest.class));
                            // 编码器
                            pipeline.addLast("encode", new RpcEncoder(kryoSerializer, RpcResponse.class));
                            // 请求处理器
                            pipeline.addLast(new NettyRpcServerHandler());
                        }
                    })
                    // 是否开启 Nagle 算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128);
            // 绑定端口
            ChannelFuture future = bootstrap.bind(host, port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 设置所有 bean 属性后调用
     */
    @Override
    public void afterPropertiesSet() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("set ApplicationContext");
        // 获得所有被 RpcService 注解的类
        Map<String, Object> registeredBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        registeredBeanMap.values().forEach(o -> registerService(o));
    }
}

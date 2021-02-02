package com.siman.qrpc.remoting.transport.socket;

import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.util.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author SiMan
 * @date 2020/12/25 1:14
 */

@Slf4j
public class SocketRpcServer {
    public static final int PORT = 9998;

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public void registerService(Object service) {
        serviceProvider.addService(service);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = "127.0.0.1";
            server.bind(new InetSocketAddress(host, PORT));
            Socket socket;
            // 获取新的连接，此处会阻塞
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}

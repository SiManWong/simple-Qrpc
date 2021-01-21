package com.siman.qrpc.proxy;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.model.RpcRequest;
import com.siman.qrpc.model.RpcResponse;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.client.NettyRpcClient;
import com.siman.qrpc.remoting.transport.socket.SocketRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 动态代理类。当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法
 * @author SiMan
 * @date 2020/12/25 2:01
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String INTERFACE_NAME = "interfaceName";

    /**
     * 用于发送请求给服务端，对应socket和netty两种实现方式
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceProperties serviceProperties;

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceProperties rpcServiceProperties) {
        this.rpcRequestTransport = rpcRequestTransport;
//        if (rpcServiceProperties.getGroup() == null) {
//            rpcServiceProperties.setGroup("");
//        }
//        if (rpcServiceProperties.getVersion() == null) {
//            rpcServiceProperties.setVersion("");
//        }
        this.serviceProperties = rpcServiceProperties;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport) {
        this.rpcRequestTransport = rpcRequestTransport;
        this.serviceProperties = RpcServiceProperties.builder().build();
    }

    /**
     * 通过 Proxy.newProxyInstance() 方法获取某个类的代理对象
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 当你使用代理对象调用方法的时候实际会调用到这个方法。代理对象就是你通过上面的 getProxy 方法获取到的对象。
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getCanonicalName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString()).build();
        RpcResponse<Object> rpcResponse = null;
        // 基于 Socket 实现
        if (rpcRequestTransport instanceof SocketRpcClient) {
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        // 基于 Netty 实现
        if (rpcRequestTransport instanceof NettyRpcClient) {
            rpcResponse = (RpcResponse<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        return rpcResponse.getData();
    }
}

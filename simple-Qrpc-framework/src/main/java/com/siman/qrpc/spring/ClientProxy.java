package com.siman.qrpc.spring;

import com.siman.qrpc.proxy.RpcClientProxy;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.client.NettyRpcClient;

/**
 * 简单写一个方法获取代理对象，其实应该在 rpc 框架里面提供一个接口获取代理对象
 * @author SiMan
 * @date 2021/2/1 0:13
 */

public class ClientProxy {
    public static <T> T getServiceProxy(Class<T> serviceClass) {
        RpcRequestTransport nettyRpcClient = new NettyRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyRpcClient);

        return rpcClientProxy.getProxy(serviceClass);
    }
}

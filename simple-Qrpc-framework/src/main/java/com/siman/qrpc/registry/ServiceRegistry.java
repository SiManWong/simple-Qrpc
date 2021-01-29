package com.siman.qrpc.registry;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/20 22:50
 */

public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param rpcServiceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}

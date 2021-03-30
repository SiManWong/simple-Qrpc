package com.siman.qrpc.registry;

import com.siman.qrpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/20 22:50
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param rpcServiceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}

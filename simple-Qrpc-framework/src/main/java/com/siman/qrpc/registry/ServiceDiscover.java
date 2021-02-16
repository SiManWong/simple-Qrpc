package com.siman.qrpc.registry;

import com.siman.qrpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/29 1:29
 */
@SPI
public interface ServiceDiscover {
    /**
     * 查找服务
     * @param serviceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress lookupService(String serviceName);
}

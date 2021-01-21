package com.siman.qrpc.registry;

/**
 * @author SiMan
 * @date 2021/1/20 22:50
 */

public interface ServiceRegistry {
    /**
     * 注册服务
     * @param service
     * @param <T>
     */
    <T> void register(T service);

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    Object getService(String serviceName);
}

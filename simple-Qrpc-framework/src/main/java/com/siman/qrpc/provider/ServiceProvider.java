package com.siman.qrpc.provider;

import com.siman.qrpc.entity.RpcServiceProperties;

/**
 * 保存和提供服务实例对象。服务端使用。
 * @author SiMan
 * @date 2020/12/28 1:53
 */
public interface ServiceProvider {
    /**
     * 添加服务
     * @param service
     */
    void addService(Object service);

    /**
     * 添加服务
     * @param service
     * @param serviceClass
     */
    void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties);
    /**
     * 获取服务
     * @param rpcServiceProperties 服务相关配置
     * @return service object
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     * @param service              service object
     * @param rpcServiceProperties service related attributes
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * 发布服务
     * @param service service object
     */
    void publishService(Object service);
}

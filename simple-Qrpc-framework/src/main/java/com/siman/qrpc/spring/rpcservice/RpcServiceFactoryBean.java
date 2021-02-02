package com.siman.qrpc.spring.rpcservice;

import com.siman.qrpc.spring.ClientProxy;
import org.springframework.beans.factory.FactoryBean;

/**
 * 实现 {@link FactoryBean} 接口，生成 rpc 服务 bean 实例
 * @author SiMan
 * @date 2021/2/1 0:11
 */

public class RpcServiceFactoryBean<T> implements FactoryBean<T> {
    private Class<T> rpcServiceInterface;

    public RpcServiceFactoryBean() {}

    public RpcServiceFactoryBean(Class<T> rpcServiceInterface) {
        this.rpcServiceInterface = rpcServiceInterface;
    }

    @Override
    public T getObject() throws Exception {
        if (rpcServiceInterface == null) {
            throw new IllegalStateException("");
        }
        return ClientProxy.getServiceProxy(rpcServiceInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return rpcServiceInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}

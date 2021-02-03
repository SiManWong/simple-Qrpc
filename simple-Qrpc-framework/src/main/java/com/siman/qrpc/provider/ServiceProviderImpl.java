package com.siman.qrpc.provider;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.enums.RpcErrorMessage;
import com.siman.qrpc.exception.RpcException;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.registry.ServiceRegistry;
import com.siman.qrpc.registry.zk.ZkServiceRegistry;
import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务注册中心实现，通过 Map 保存服务信息，可以通过 zookeeper 来改进
 * 实现了 ServiceProvider 接口，可以将其看做是一个保存和提供服务实例对象的示例
 * @author SiMan
 * @date 2020/12/28 1:57
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider{
    /**
     * 接口名和服务的对应关系，TODO 处理一个接口被两个实现类实现的情况
     * key:service/interface name
     * value:service
     */
    private final Map<String, Object> serviceMap;

    /**
     * 已注册的服务
     */
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = SingletonFactory.getInstance(ZkServiceRegistry.class);
    }

    @Override
    public void addService(Object service) {
        Class<?> serviceClass = service.getClass().getInterfaces()[0];
        this.addService(service, serviceClass, RpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void addService(Object service, Class<?> serviceClass, RpcServiceProperties rpcServiceProperties) {
        String rpcServiceName = rpcServiceProperties.toRpcServiceName();
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = serviceMap.get(rpcServiceProperties.toRpcServiceName());
        if (null == service) {
            throw new RpcException(RpcErrorMessage.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            Class<?> anInterface = service.getClass().getInterfaces()[0];
            String serviceName = anInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);
            this.addService(service, anInterface, rpcServiceProperties);
            serviceRegistry.registerService(serviceName, new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }


}

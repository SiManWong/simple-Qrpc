package com.siman.qrpc.provider;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.enums.RpcErrorMessageEnum;
import com.siman.qrpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
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
//    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
//        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
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
    public void addService(Object service) {
        Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
        String rpcServiceName = serviceRelatedInterface.getCanonicalName();
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
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String realHost = InetAddress.getLocalHost().getHostAddress();
            String host = "127.0.0.1";
            Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
            String serviceName = serviceRelatedInterface.getCanonicalName();
            rpcServiceProperties.setServiceName(serviceName);
            this.addService(service, serviceRelatedInterface, rpcServiceProperties);
            //TODO 注册中心注册服务
//            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}

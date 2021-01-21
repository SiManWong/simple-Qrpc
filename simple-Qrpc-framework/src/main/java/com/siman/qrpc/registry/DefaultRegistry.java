package com.siman.qrpc.registry;

import com.siman.qrpc.enums.RpcErrorMessageEnum;
import com.siman.qrpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SiMan
 * @date 2021/1/20 22:51
 */
@Slf4j
public class DefaultRegistry implements ServiceRegistry{

    /**
     * key:service/interface name
     * value:service
     */
    private final static Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private final static Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * TODO 修改为扫描注解注册
     */
    @Override
    public <T> void register(T service) {
        String serviceName = service.getClass().getCanonicalName();
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        log.info("Add service: {} and interfaces:{}", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}

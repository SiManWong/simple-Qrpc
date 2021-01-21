package com.siman.qrpc.handler;

import com.siman.qrpc.exception.RpcException;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.model.RpcRequest;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author SiMan
 * @date 2020/12/28 1:41
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    /**
     * 处理 rpc 请求: 调用对应的方法并返回
     */
    public Object handle(RpcRequest rpcRequest) {
        // 通过服务配置信息从服务提供者获取对应服务
        Object service = serviceProvider.getService(rpcRequest.toRpcProperties());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 调用目标方法
     *
     * @param rpcRequest 客户端请求
     * @param service    服务端
     * @return 目标方法返回的结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            // 根据方法名和参数 获取服务对应的方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            // 执行相应的方法
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}

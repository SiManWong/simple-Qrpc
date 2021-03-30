package com.siman.qrpc.spring;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.extension.ExtensionLoader;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.proxy.RpcClientProxy;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.client.NettyRpcClient;
import com.siman.qrpc.spring.annotation.Reference;
import com.siman.qrpc.spring.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


/**
 * 初始化 bean 的前置处理
 * @author SiMan
 * @date 2021/2/2 21:37
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor () {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        /*
            发布服务
         */
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取 RpcService 注解
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            // 构建Rpc服务属性
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group(annotation.group())
                    .version(annotation.version()).build();
            serviceProvider.publishService(bean, rpcServiceProperties);
        }

        /*
            消费服务
         */
        Class<?> targetClass = bean.getClass();
        // 获取类中所有字段
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            Reference reference = declaredField.getAnnotation(Reference.class);
            if (reference != null) {
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .version(reference.version()).group(reference.group()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                // 允许访问私有属性
                declaredField.setAccessible(true);
                try {
                    // 给bean注入实现类
                    declaredField.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return bean;
    }

}

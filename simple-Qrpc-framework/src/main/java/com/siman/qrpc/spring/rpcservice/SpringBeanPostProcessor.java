package com.siman.qrpc.spring.rpcservice;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.factory.SingletonFactory;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.registry.ServiceRegistry;
import com.siman.qrpc.registry.zk.ZkServiceRegistry;
import com.siman.qrpc.spring.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


/**
 * 初始化 bean 的前置处理
 * @author SiMan
 * @date 2021/2/2 21:37
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;

    public SpringBeanPostProcessor () {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().group(annotation.group())
                    .version(annotation.version()).build();
            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }
}

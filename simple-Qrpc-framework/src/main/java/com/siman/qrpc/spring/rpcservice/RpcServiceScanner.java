package com.siman.qrpc.spring.rpcservice;

import com.siman.qrpc.spring.annotation.RpcServiceScan;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * {@link ClassPathBeanDefinitionScanner} bean 扫描器，通过扫描器可获取我们需要注册的bean
 * @author SiMan
 * @date 2021/2/1 0:09
 */

public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {
    private RpcServiceFactoryBean<Object> rpcServiceFactoryBean = new RpcServiceFactoryBean<>();

    public RpcServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        // 被扫描的路径下包含这个注解的类会被加载到Spring容器中
        // 父类在扫描的时候 beanDefinition 会通过 registry 注册，我们需要修改 beanDefinition 的 beanClass
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        processBeanDefinitions(beanDefinitionHolders);

        return beanDefinitionHolders;
    }

    /**
     * 主要是将 beanDefinition 的 beanClass 设置成我们自定义的 FactoryBean
     *
     * @param beanDefinitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitions) {
            definition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();

            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());

            definition.setBeanClass(rpcServiceFactoryBean.getClass());

            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }

    /**
     * @addIncludeFilter 将自定义的注解添加到扫描任务中
     * @addExcludeFilter 将带有自定义注解的类 ，不加载到容器中
     */
    protected void registerFilters() {
        // 传入注解过滤器 AnnotationTypeFilter
        addIncludeFilter(new AnnotationTypeFilter(RpcServiceScan.class));
        addExcludeFilter(new AnnotationTypeFilter(RpcServiceScan.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }
}

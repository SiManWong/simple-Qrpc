package com.siman.qrpc.spring.reference;

import com.siman.qrpc.spring.annotation.Reference;
import com.siman.qrpc.spring.reference.rpcservice.RpcServiceScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

/**
 * @author SiMan
 * @date 2021/1/31 23:56
 */

public class RpcServiceScannerRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        /**
         * 获取数据结构为 Map 的属性集
         * key：属性名
         * value：属性值
         */
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(Reference.class.getName()));
        RpcServiceScanner scanner = new RpcServiceScanner(registry);
        // 获取属性名为 value 的值
        String value = annotationAttributes.getString("value");
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        // 所有的接口全部注入
        scanner.addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> true);
        scanner.doScan(value);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

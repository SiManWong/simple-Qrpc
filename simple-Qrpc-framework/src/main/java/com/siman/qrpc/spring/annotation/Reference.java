package com.siman.qrpc.spring.annotation;

import com.siman.qrpc.spring.reference.RpcServiceScannerRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * @author SiMan
 * @date 2021/1/31 23:55
 * @see Target 用于描述注解可应用于哪种元素 {@link ElementType#TYPE} 表示类，接口或枚举
 * @see Import 引入实现了 {@ImportSelector} 和 {@ImportBeanDefinitionRegistrar} 的特殊 Bean
 * （仅仅是引入，不会被 Spring 容器管理）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcServiceScannerRegister.class)
public @interface Reference {
    String value();
}
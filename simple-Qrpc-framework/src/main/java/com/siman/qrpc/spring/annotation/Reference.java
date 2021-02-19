package com.siman.qrpc.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RPC reference annotation
 * @author SiMan
 * @date 2021/2/17 16:40
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Reference {
    /**
     * 服务版本号
     */
    String version() default "";

    /**
     * 服务组别
     */
    String group() default "";
}

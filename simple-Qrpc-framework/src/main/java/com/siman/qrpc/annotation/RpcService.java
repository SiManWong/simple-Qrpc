package com.siman.qrpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author SiMan
 * @date 2021/2/2 0:35
 */
@Component
@Inherited
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    /**
     * 服务版本号
     * @return
     */
    String version() default "";

    /**
     * 服务组
     * @return
     */
    String group() default "";
}

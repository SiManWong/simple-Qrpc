package com.siman.qrpc.spring.annotation;


import com.siman.qrpc.spring.scanner.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * scan custom annotations
 * @author SiMan
 * @date 2021/2/16 14:33
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[]basePackage();
}

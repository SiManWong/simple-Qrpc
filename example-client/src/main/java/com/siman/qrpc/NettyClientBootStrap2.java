package com.siman.qrpc;

import com.siman.qrpc.spring.annotation.Reference;
import com.siman.qrpc.spring.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author SiMan
 * @date 2021/2/2 1:40
 */

@RpcScan(basePackage = {"com.siman.qrpc"})
public class NettyClientBootStrap2 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientBootStrap2.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}

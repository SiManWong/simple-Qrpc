package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.spring.annotation.RpcService;

/**
 * @author SiMan
 * @date 2021/2/2 21:17
 */
@RpcService(group = "test2", version = "1.0")
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        String result = "Hello description is " +hello.getDescription();

        return result;
    }
}

package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.spring.annotation.RpcService;
import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;

/**
 * @author SiMan
 * @date 2021/1/17 2:08
 */

@RpcService(group = "test1", version = "1.0")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        String result = "Hello description is " +hello.getDescription();

        return result;
    }
}

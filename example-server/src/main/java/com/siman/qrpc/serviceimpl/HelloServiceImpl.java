package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.annotation.RpcService;
import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;

/**
 * @author SiMan
 * @date 2021/1/17 2:08
 */

@RpcService
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("sdasdasdasdasd");
    }

    @Override
    public String hello(Hello hello) {
        String result = "Hello description is " +hello.getDescription();

        return result;
    }
}

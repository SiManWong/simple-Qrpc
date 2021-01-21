package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;

/**
 * @author SiMan
 * @date 2021/1/17 2:08
 */

public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(Hello hello) {
        String result = hello.getMessage();

        return result;
    }
}

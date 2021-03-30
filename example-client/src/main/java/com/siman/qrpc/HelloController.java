package com.siman.qrpc;

import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.spring.annotation.Reference;
import org.springframework.stereotype.Component;

/**
 * @author SiMan
 * @date 2021/2/18 19:46
 */
@Component
public class HelloController {
    @Reference(version = "1.0", group = "test1")
    private HelloService helloService;

    public void test() {
        String hello = helloService.hello(new Hello("111", "222"));
        assert "Hello description is 222".equals(hello);
    }
}

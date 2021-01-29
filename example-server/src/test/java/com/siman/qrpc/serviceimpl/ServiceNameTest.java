package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.service.HelloService;
import org.junit.jupiter.api.Test;

/**
 * @author SiMan
 * @date 2021/1/18 21:09
 */

public class ServiceNameTest {
    @Test
    void serviceName() {
        HelloService service = new HelloServiceImpl();
        Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
        String serviceName = serviceRelatedInterface.getCanonicalName();

        System.out.println(serviceName);
    }

    @Test
    void interfaceName() {
        HelloService service = new HelloServiceImpl();
        System.out.println("HelloService.class: " + HelloService.class);
        System.out.println("service.getClass(): " + service.getClass());
        System.out.println("service.getClass().getInterfaces()[0].getCanonicalName(): " + service.getClass().getInterfaces()[0].getCanonicalName());
        System.out.println("service.getClass().getInterfaces()[0].getCanonicalName(): " + service.getClass().getInterfaces()[0].getCanonicalName());
        System.out.println();
    }
}

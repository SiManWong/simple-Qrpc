package com.siman.qrpc.serviceimpl;

import com.siman.qrpc.service.HelloService;
import org.junit.Test;

/**
 * @author SiMan
 * @date 2021/1/18 21:09
 */

public class ServiceNameTest {
    @Test
    public void serviceName() {
        HelloService service = new HelloServiceImpl();
        Class<?> serviceRelatedInterface = service.getClass().getInterfaces()[0];
        String serviceName = serviceRelatedInterface.getCanonicalName();

        System.out.println(serviceName);
    }
}

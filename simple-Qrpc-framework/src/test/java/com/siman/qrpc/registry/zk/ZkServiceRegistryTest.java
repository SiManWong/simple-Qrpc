package com.siman.qrpc.registry.zk;

import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.registry.ServiceRegistry;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author SiMan
 * @date 2021/1/29 1:38
 */

public class ZkServiceRegistryTest {
    @Test
    void registry_and_lookup() {
        ServiceRegistry serviceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 4396);
        serviceRegistry.registerService("com.siman.qrpc.registry,ZkServiceRegistry", givenInetSocketAddress);
        ServiceDiscover serviceDiscover = new ZkServiceDiscover();
        InetSocketAddress inetSocketAddress = serviceDiscover.lookupService("com.siman.qrpc.registry,ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), inetSocketAddress.toString());
    }
}

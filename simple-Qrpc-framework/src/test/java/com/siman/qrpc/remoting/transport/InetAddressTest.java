package com.siman.qrpc.remoting.transport;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author SiMan
 * @date 2021/1/17 1:45
 */

public class InetAddressTest {
    @Test
    void getHostAddress() {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println(hostAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}

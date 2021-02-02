package com.siman.qrpc.registry.zk;

import com.siman.qrpc.loadbalance.LoadBalance;
import com.siman.qrpc.loadbalance.RandomLoadBalance;
import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author SiMan
 * @date 2021/1/29 1:31
 */
@Slf4j
public class ZkServiceDiscover implements ServiceDiscover {
    private final LoadBalance loadBalance;

     public ZkServiceDiscover() {
         loadBalance = new RandomLoadBalance();
     }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        List<String> serviceAddresses = CuratorUtils.getChildrenNodes(serviceName);
        // 负载均衡
        String serviceAddress = loadBalance.selectServiceAddress(serviceAddresses);

        log.info("成功找到服务地址:{}", serviceAddress);

        String[] socketAddressArray = serviceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);

        return new InetSocketAddress(host, port);
    }
}

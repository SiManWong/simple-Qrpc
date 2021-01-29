package com.siman.qrpc.registry.zk;

import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/29 1:31
 */
@Slf4j
public class ZkServiceDiscover implements ServiceDiscover {
    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO:feat:负载均衡
        // 这里直接去了第一个找到的服务地址
        String serviceAddress = CuratorUtils.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址:{}", serviceAddress);

        return new InetSocketAddress(serviceAddress.split(":")[0], Integer.parseInt(serviceAddress.split(":")[1]));
    }
}

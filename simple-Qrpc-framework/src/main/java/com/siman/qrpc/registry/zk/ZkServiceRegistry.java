package com.siman.qrpc.registry.zk;

import com.siman.qrpc.registry.ServiceRegistry;
import com.siman.qrpc.registry.zk.util.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/29 1:31
 */

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // 根节点下注册子节点：服务
        StringBuilder servicePath = new StringBuilder(CuratorUtils.ZK_REGISTER_ROOT_PATH).append("/").append(rpcServiceName);
        // 服务子节点下注册子节点：服务地址
        servicePath.append(inetSocketAddress.toString());
        CuratorUtils.createEphemeralNode(servicePath.toString());
    }
}

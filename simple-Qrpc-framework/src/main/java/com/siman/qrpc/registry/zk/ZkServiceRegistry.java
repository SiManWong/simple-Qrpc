package com.siman.qrpc.registry.zk;

import com.siman.qrpc.registry.ServiceRegistry;
import com.siman.qrpc.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @author SiMan
 * @date 2021/1/29 1:31
 */

public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createEphemeralNode(zkClient, servicePath);
    }
}

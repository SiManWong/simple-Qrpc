package com.siman.qrpc.registry.zk.util;

import com.siman.qrpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SiMan
 * @date 2021/1/29 0:58
 */
@Slf4j
public final class CuratorUtils {
    /**
     * 重试间隔初始时间
     */
    private static final int BASE_SLEEP_TIME = 1000;
    /**
     * 最大重试次数
     */
    private static final int MAX_RETRIES = 3;
    /**
     * 根目录
     */
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final String CONNECT_STRING = "120.24.61.20:2181";
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private static CuratorFramework zkClient;

    static {
        zkClient = getZkClient();
    }

    private CuratorUtils() {}

    public static CuratorFramework getZkClient() {
        // 重试策略。重试3次，并且会增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();
        return curatorFramework;
    }

    /**
     * 创建临时节点
     * 临时节点贮存在 Zookeeper 中，当连接和 session 断掉时被删除
     * @param path 节点路径
     */
    public static void createEphemeralNode(String path) {
        try {
            if (zkClient.checkExists().forPath(path) == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            } else {
                log.info("节点已经存在，节点为:[{}]", path);
            }
        } catch (Exception exception) {
            throw new RpcException(exception.getMessage(), exception.getCause());
        }
    }

    /**
     * 获取某个节点下的子节点，也就是获取所有提供服务的生产者的地址
     *
     * @param serviceName 服务对象接口名 eg:com.siman.qrpc.service.HelloService
     * @return 指定节点下的所有子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, result);
            registerWatch(zkClient, serviceName);
        } catch (Exception exception) {
            throw new RpcException(exception.getMessage(), exception.getCause());
        }

        return result;
    }

    /**
     * 注册监听指定节点
     *
     * @param serviceName 服务对象接口名 eg:
     */
    public static void registerWatch(CuratorFramework zkClient, String serviceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception exception) {
            log.error("occur exception", exception);
        }
    }
}

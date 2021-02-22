package com.siman.qrpc.registry.zk.util;

import com.siman.qrpc.enums.RpcConfigEnum;
import com.siman.qrpc.exception.RpcException;
import com.siman.qrpc.util.file.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * zookeeper 客户端 Curator 工具类
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
    private static String defaultZookeeperAddress = "127.0.0.1:2181";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;

    private CuratorUtils() {}

    public static CuratorFramework getZkClient() {
        // 通过配置文件获取 zookeeper 地址
        Properties properties = null;
        properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        if (properties != null) {
            defaultZookeeperAddress = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        }
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略。重试3次，并且会增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(defaultZookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    /**
     * 创建临时节点
     * 临时节点贮存在 Zookeeper 中，当连接和 session 断掉时被删除
     * @param path 节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点已经存在，节点为:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点创建成功，节点为:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception exception) {
            log.error("创建持久节点 [{}] 失败", path);
        }
    }

    /**
     * 获取某个节点下的子节点，也就是获取所有提供服务的生产者的地址
     *
     * @param serviceName 服务对象接口名 eg:com.siman.qrpc.service.HelloService
     * @return 指定节点下的所有子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String serviceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(serviceName)) {
            return SERVICE_ADDRESS_MAP.get(serviceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, result);
            registerWatcher(serviceName, zkClient);
        } catch (Exception exception) {
            log.error(" [{}] 获取子节点失败", servicePath);
        }

        return result;
    }

    /**
     * 清空注册中心
     * @param zkClient
     */
    public static void clearRegistry(CuratorFramework zkClient) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception exception) {
                log.error("清除节点:[{}] 失败", p);
            }
        });
        log.info("清除注册中心的节点:[{}]", REGISTERED_PATH_SET.toString());
    }

    /**
     * 注册监听指定节点
     *
     * @param serviceName 服务对象接口名 eg:
     */
    private static void registerWatcher(String serviceName, CuratorFramework zkClient) throws Exception{
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(serviceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }
}

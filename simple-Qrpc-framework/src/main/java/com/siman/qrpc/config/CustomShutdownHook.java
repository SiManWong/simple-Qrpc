package com.siman.qrpc.config;

import com.siman.qrpc.registry.zk.util.CuratorUtils;
import com.siman.qrpc.util.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 关闭服务器时，注销所有服务
 * @author SiMan
 * @date 2021/1/31 2:01
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry(CuratorUtils.getZkClient());
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}

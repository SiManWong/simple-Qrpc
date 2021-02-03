package com.siman.qrpc.enums;

/**
 * @author SiMan
 * @date 2021/2/2 20:45
 */

public enum RpcProperties {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");


    private String propertyValue;

    RpcProperties(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValue() {
        return propertyValue;
    }
}

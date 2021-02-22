package com.siman.qrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author SiMan
 * @date 2021/2/2 20:45
 */
@Getter
@AllArgsConstructor
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");


    private String propertyValue;
}

package com.siman.qrpc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author SiMan
 * @date 2021/1/18 1:55
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcServiceProperties {
    private String serviceName;

    public String toRpcServiceName() {
        return this.getServiceName();
    }
}

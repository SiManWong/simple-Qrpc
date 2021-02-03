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
    /**
     * 服务版本号
     */
    private String version;
    /**
     * 一个接口有多个实现类时，按组区分
     */
    private String group;

    /**
     * @return 最终的服务名
     */
    public String toRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }
}

package com.siman.qrpc.model;

import com.siman.qrpc.entity.RpcServiceProperties;
import lombok.*;

import java.io.Serializable;

/**
 * Rpc 请求实体类
 * @author SiMan
 * @date 2020/12/25 18:09
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public RpcServiceProperties toRpcProperties() {
        return RpcServiceProperties.builder().serviceName(this.getInterfaceName()).build();
    }
}

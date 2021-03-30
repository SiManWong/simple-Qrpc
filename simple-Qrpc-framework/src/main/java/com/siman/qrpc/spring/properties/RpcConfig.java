package com.siman.qrpc.spring.properties;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author SiMan
 * @date 2021/2/23 17:48
 */
@Data
public class RpcConfig {
    /**
     * 序列化方式
     */
    private String serialization;
}

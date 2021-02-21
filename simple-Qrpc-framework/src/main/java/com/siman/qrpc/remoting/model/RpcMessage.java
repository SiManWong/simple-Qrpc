package com.siman.qrpc.remoting.model;

import lombok.*;

/**
 * @author SiMan
 * @date 2021/2/19 23:54
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * 消息类型
     */
    private byte messageType;

    /**
     * 序列化类型
     */
    private byte codec;

    /**
     * 请求id
     */
    private int requestId;

    /**
     * 数据内容
     */
    private Object data;
}

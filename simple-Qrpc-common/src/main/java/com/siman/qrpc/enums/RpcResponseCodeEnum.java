package com.siman.qrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author SiMan
 * @date 2020/12/25 18:13
 */
@ToString
@Getter
@AllArgsConstructor
public enum RpcResponseCodeEnum {

    // 成功
    SUCCESS(200, "调用方法成功"),
    // 失败
    FAIL(500, "调用方法失败");

    private final int code;

    private final String message;
}
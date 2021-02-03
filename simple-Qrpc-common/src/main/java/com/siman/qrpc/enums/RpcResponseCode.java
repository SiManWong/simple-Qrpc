package com.siman.qrpc.enums;

import lombok.ToString;

/**
 * @author SiMan
 * @date 2020/12/25 18:13
 */
@ToString
public enum RpcResponseCode {

    // 成功
    SUCCESS(200, "调用方法成功"),
    // 失败
    FAIL(500, "调用方法失败");

    private final int code;

    private final String message;

    RpcResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
package com.siman.qrpc.enums;

import lombok.ToString;

/**
 * @author SiMan
 * @date 2020/12/25 18:13
 */
@ToString
public enum RpcResponseCodeEnum {

    // 成功
    SUCCESS(200, "The remote call is successful"),
    // 失败
    FAIL(500, "The remote call is fail");

    private final int code;

    private final String message;

    RpcResponseCodeEnum(int code, String message) {
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
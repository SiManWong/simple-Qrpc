package com.siman.qrpc.exception;

import com.siman.qrpc.enums.RpcErrorMessage;

/**
 * @author SiMan
 * @date 2020/12/25 1:38
 */

public class RpcException extends RuntimeException {
    public RpcException(RpcErrorMessage rpcErrorMessage, String detail) {
        super(rpcErrorMessage.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessage rpcErrorMessage) {
        super(rpcErrorMessage.getMessage());
    }
}
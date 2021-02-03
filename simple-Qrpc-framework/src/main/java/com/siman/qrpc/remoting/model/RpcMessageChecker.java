package com.siman.qrpc.remoting.model;

import com.siman.qrpc.enums.RpcErrorMessage;
import com.siman.qrpc.enums.RpcResponseCode;
import com.siman.qrpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author SiMan
 * @date 2021/1/28 23:41
 */
@Slf4j
public class RpcMessageChecker {
    private static final String INTERFACE_NAME = "interfaceName";

    private RpcMessageChecker() {}

    public static void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessage.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}

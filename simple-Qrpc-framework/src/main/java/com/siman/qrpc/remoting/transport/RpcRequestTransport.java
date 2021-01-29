package com.siman.qrpc.remoting.transport;

import com.siman.qrpc.remoting.model.RpcRequest;

/**
 * @author SiMan
 * @date 2020/12/17 1:11
 */
public interface RpcRequestTransport {
    /**
     * 发送 rpc请求到服务器并获取结果
     * @return
     * @param rpcRequest
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}

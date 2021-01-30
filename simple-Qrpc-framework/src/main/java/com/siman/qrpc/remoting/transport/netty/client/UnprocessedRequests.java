package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.remoting.model.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SiMan
 * @date 2021/1/30 17:38
 */

public class UnprocessedRequests {
    private static Map<String, CompletableFuture<RpcResponse>> unprocessedResponseFuture = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        unprocessedResponseFuture.put(requestId, future);
    }

    public void complete(RpcResponse rpcResponse) {
        CompletableFuture<RpcResponse> future = unprocessedResponseFuture.remove(rpcResponse.getRequestId());
        if (null != future) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}

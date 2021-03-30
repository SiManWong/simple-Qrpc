package com.siman.qrpc.remoting.transport.socket;

import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.exception.RpcException;
import com.siman.qrpc.extension.ExtensionLoader;
import com.siman.qrpc.registry.ServiceDiscover;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author SiMan
 * @date 2020/12/25 1:42
 */
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscover serviceDiscover;
    public SocketRpcClient() {
        this.serviceDiscover = ExtensionLoader.getExtensionLoader(ServiceDiscover.class).getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        String rpcServiceName = RpcServiceProperties.builder().serviceName(rpcRequest.getInterfaceName())
                .group(rpcRequest.getGroup()).version(rpcRequest.getVersion()).build().toRpcServiceName();
        InetSocketAddress inetSocketAddress = serviceDiscover.lookupService(rpcServiceName);
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流发送 rpcRequest 到服务端
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 从输入流获取服务端发送的数据
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("调用服务失败:", e);
        }
    }
}

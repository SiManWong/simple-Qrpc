import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.remoting.transport.socket.SocketRpcServer;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.serviceimpl.HelloServiceImpl;

/**
 * @author SiMan
 * @date 2021/1/17 1:56
 */

public class SocketServerBootStrap {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServiceProperties rpcServiceProperties = new RpcServiceProperties();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
        socketRpcServer.registerService(helloService);
        socketRpcServer.start();
    }
}

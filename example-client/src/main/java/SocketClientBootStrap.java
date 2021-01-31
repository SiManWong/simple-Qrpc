import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.proxy.RpcClientProxy;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.socket.SocketRpcClient;
import com.siman.qrpc.service.HelloService;

/**
 * @author SiMan
 * @date 2021/1/17 1:59
 */

public class SocketClientBootStrap {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new SocketRpcClient();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceProperties);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        for (int i = 0; i < 50; i++) {
            String hello = helloService.hello(new Hello("消息", "描述"));
            System.out.println(hello);
        }
    }
}

import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.proxy.RpcClientProxy;
import com.siman.qrpc.remoting.transport.RpcRequestTransport;
import com.siman.qrpc.remoting.transport.netty.client.NettyRpcClient;
import com.siman.qrpc.service.HelloService;

/**
 * @author SiMan
 * @date 2021/1/23 0:24
 */

public class NettyClientBootStrap {
    public static void main(String[] args) {
        RpcRequestTransport rpcRequestTransport = new NettyRpcClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("消息", "描述"));
        System.out.println(hello);
    }
}

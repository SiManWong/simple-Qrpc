import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import com.siman.qrpc.serviceimpl.HelloServiceImpl;

/**
 * @author SiMan
 * @date 2021/1/23 0:02
 */

public class NettyServerBootStrap {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.registerService(helloService);
        nettyRpcServer.start();
    }
}

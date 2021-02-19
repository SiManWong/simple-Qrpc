import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import com.siman.qrpc.spring.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 通过注解注册服务
 * @author SiMan
 * @date 2021/1/23 0:02
 */
@RpcScan(basePackage = {"com.siman.qrpc.serviceimpl"})
public class NettyServerBootStrap {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(NettyServerBootStrap.class);
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.start();
    }
}
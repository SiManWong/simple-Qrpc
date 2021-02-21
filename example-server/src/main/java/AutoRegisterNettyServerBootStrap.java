import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import com.siman.qrpc.spring.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 通过注解注册服务
 * @author SiMan
 * @date 2021/1/23 0:02
 */
@RpcScan(basePackage = {"com.siman.qrpc"})
public class AutoRegisterNettyServerBootStrap {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AutoRegisterNettyServerBootStrap.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        nettyRpcServer.start();
    }
}
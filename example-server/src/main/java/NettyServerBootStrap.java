import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 通过注解注册服务
 * @author SiMan
 * @date 2021/1/23 0:02
 */
@ComponentScan("com.siman.qrpc")
public class NettyServerBootStrap {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerBootStrap.class);
        NettyRpcServer nettyRpcServer = applicationContext.getBean(NettyRpcServer.class);
        nettyRpcServer.start();
    }
}

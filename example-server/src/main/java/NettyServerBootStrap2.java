import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.serviceimpl.HelloServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 手动注册服务
 * @author SiMan
 * @date 2021/2/1 21:44
 */

public class NettyServerBootStrap2 {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerBootStrap.class);
        NettyRpcServer nettyRpcServer = applicationContext.getBean(NettyRpcServer.class);
        nettyRpcServer.start();

        ServiceProvider serviceProvider = new ServiceProviderImpl();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test").version("1").build();
        serviceProvider.publishService(helloService, rpcServiceProperties);
    }
}

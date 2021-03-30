import com.siman.qrpc.entity.RpcServiceProperties;
import com.siman.qrpc.provider.ServiceProvider;
import com.siman.qrpc.provider.ServiceProviderImpl;
import com.siman.qrpc.remoting.transport.netty.server.NettyRpcServer;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.serviceimpl.HelloServiceImpl;
import com.siman.qrpc.spring.annotation.RpcScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 手动注册服务
 * @author SiMan
 * @date 2021/2/1 21:44
 */
@RpcScan(basePackage = {"com.siman.qrpc"})
public class ManuallyRegisterNettyServerBootStrap {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AutoRegisterNettyServerBootStrap.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer)applicationContext.getBean("nettyRpcServer");

        HelloService helloService = new HelloServiceImpl();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test").version("2.0").build();
        ServiceProvider serviceProvider = new ServiceProviderImpl();
        serviceProvider.publishService(helloService, rpcServiceProperties);
        nettyRpcServer.start();
    }
}

import com.siman.qrpc.pojo.Hello;
import com.siman.qrpc.service.HelloService;
import com.siman.qrpc.spring.annotation.Reference;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author SiMan
 * @date 2021/2/2 1:40
 */

@Reference("com.siman.qrpc.service")
public class NettyClientBootStrap2 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientBootStrap2.class);
        HelloService helloService = applicationContext.getBean(HelloService.class);
        Hello hello = Hello.builder().message("消息").description("描述").build();
        helloService.hello(hello);

    }
}

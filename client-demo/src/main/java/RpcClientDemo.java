import com.sbss.bithon.component.brpc.channel.ClientChannel;
import com.sbss.bithon.component.brpc.example.IExampleService;
import com.sbss.bithon.component.brpc.example.INotification;
import com.sbss.bithon.component.brpc.exception.ServiceInvocationException;
import com.sbss.bithon.component.brpc.invocation.ServiceStubFactory;

import java.time.Duration;
import java.util.Scanner;

public class RpcClientDemo {

    private static final String host = "127.0.0.1";

    public static void main(String[] args) {

        ClientChannel channelProvider = new ClientChannel(host, 8070)
            .bindService(INotification.class,
                         new INotification() {
                             @Override
                             public void notify(String message) {
                                 System.out.println(
                                     "Notification:"
                                     + message);
                             }
                         })
            .configureRetry(60, Duration.ofMillis(500));

        //channelProvider.debug(true);
        IExampleService exampleService = ServiceStubFactory.create(channelProvider, IExampleService.class);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("method=");
            String method = scanner.nextLine();
            if ("block".equals(method)) {
                System.out.print("timeout=");
                int timeout = scanner.nextInt();
                try {
                    exampleService.block(timeout);
                } catch (ServiceInvocationException e) {
                    System.out.println(e);
                }
            } else if ("div".equals(method)) {
                System.out.print("a=");
                int a = scanner.nextInt();
                System.out.print("b=");
                int b = scanner.nextInt();

                long s = System.currentTimeMillis();
                for (int i = 0; i < 1000; i++) {
                    int c = exampleService.div(a, b);
                }
                long e = System.currentTimeMillis();
                System.out.printf("time=%d", e - s);

            }
        }
    }
}
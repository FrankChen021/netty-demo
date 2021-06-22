import cn.bithon.rpc.channel.ClientChannel;
import cn.bithon.rpc.example.ICalculator;
import cn.bithon.rpc.example.INotification;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.invocation.ServiceStubFactory;

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
        ICalculator calculator = ServiceStubFactory.create(channelProvider, ICalculator.class);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("method=");
            String method = scanner.nextLine();
            if ("block".equals(method)) {
                System.out.print("timeout=");
                int timeout = scanner.nextInt();
                try {
                    calculator.block(timeout);
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
                    int c = calculator.div(a, b);
                }
                long e = System.currentTimeMillis();
                System.out.printf("time=%d", e - s);

            }
        }
    }
}
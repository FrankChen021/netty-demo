import cn.bithon.rpc.core.channel.ClientChannelProvider;
import cn.bithon.rpc.core.ServiceRegistry;
import cn.bithon.rpc.core.ServiceStubBuilder;
import cn.bithon.rpc.core.example.ICalculator;
import cn.bithon.rpc.core.example.INotification;

import java.util.Scanner;

public class RpcClientDemo {

    private static String host = "127.0.0.1";
    private static int MAX_RETRY = 5;

    public static void main(String[] args) {
        ServiceRegistry.register(INotification.class, new INotification() {
            @Override
            public void notify(String message) {
                System.out.println("Notification:" + message);
            }
        });

        ClientChannelProvider channelProvider = new ClientChannelProvider(host, 8070);
        //channelProvider.debug(true);
        ICalculator calculator = ServiceStubBuilder.create(channelProvider, ICalculator.class);
        Scanner scanner = new Scanner(System.in);
        while (true) {
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
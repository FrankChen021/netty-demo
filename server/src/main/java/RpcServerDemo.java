import cn.bithon.rpc.channel.ServerChannel;
import cn.bithon.rpc.example.ICalculator;
import cn.bithon.rpc.example.INotification;

import java.util.Scanner;

public class RpcServerDemo {
    public static void main(String[] args) {
        ServerChannel serverChannel = new ServerChannel()
            .addService(ICalculator.class, new ICalculator() {

                @Override
                public int div(int a, int b) {
                    return a / b;
                }

                @Override
                public int block(int timeout) {
                    try {
                        Thread.sleep(timeout * 1000L);
                    } catch (InterruptedException e) {
                    }
                    return 0;
                }
            }).start(8070);


        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                serverChannel.shutdown();
                break;
            }

            for (String clientEndpoint : serverChannel.getClientEndpoints()) {
                INotification notification = serverChannel.getRemoteService(clientEndpoint, INotification.class);
                notification.notify(line);
            }
        }
    }
}

import cn.bithon.rpc.channel.ServerChannelManager;
import cn.bithon.rpc.example.ICalculator;
import cn.bithon.rpc.example.INotification;

import java.util.Scanner;

public class RpcServerDemo {
    public static void main(String[] args) {
        ServerChannelManager serverChannelManager = new ServerChannelManager()
            .addService(ICalculator.class, new ICalculator() {
                @Override
                public int div(int a, int b) {
                    return a / b;
                }

                @Override
                public int block(int timeout) {
                    try {
                        Thread.sleep(timeout * 1000);
                    } catch (InterruptedException e) {
                    }
                    return 0;
                }
            }).start(8070);


        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                serverChannelManager.shutdown();
                break;
            }

            for (String clientEndpoint : serverChannelManager.getClientEndpoints()) {
                INotification notification = serverChannelManager.getServiceStub(clientEndpoint, INotification.class);
                notification.notify(line);
            }
        }
    }
}

import cn.bithon.rpc.core.ServiceHost;
import cn.bithon.rpc.core.example.ICalculator;
import cn.bithon.rpc.core.example.INotification;

import java.util.Scanner;

public class RpcServerDemo {
    public static void main(String[] args) {
        ServiceHost serviceHost = new ServiceHost()
            .addService(ICalculator.class, new ICalculator() {
                @Override
                public int div(int a, int b) {
                    return a / b;
                }
            }).start(8070);


        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                serviceHost.stop();
                break;
            }

            for (String clientEndpoint : serviceHost.getClientEndpoints()) {
                INotification notification = serviceHost.getServiceStub(clientEndpoint, INotification.class);
                notification.notify(line);
            }
        }
    }
}

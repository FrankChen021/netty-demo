import com.sbss.bithon.component.brpc.channel.ServerChannel;
import com.sbss.bithon.component.brpc.endpoint.EndPoint;
import com.sbss.bithon.component.brpc.example.ExampleServiceImpl;
import com.sbss.bithon.component.brpc.example.IExampleService;
import com.sbss.bithon.component.brpc.example.INotification;

import java.util.Scanner;

public class RpcServerDemo {
    public static void main(String[] args) {
        ServerChannel serverChannel = new ServerChannel()
            .bindService(IExampleService.class, new ExampleServiceImpl()).start(8070);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                serverChannel.close();
                break;
            }

            for (EndPoint clientEndpoint : serverChannel.getClientEndpoints()) {
                INotification notification = serverChannel.getRemoteService(clientEndpoint, INotification.class);
                notification.notify(line);
            }
        }
    }
}

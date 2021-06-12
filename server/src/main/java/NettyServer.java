import java.util.Scanner;

public class NettyServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer()
            .addService(ICalculator.class, new ICalculator() {
                @Override
                public int add(int a, int b) {
                    return a + b;
                }

                @Override
                public int div(int a, int b) {
                    return a / b;
                }
            }).start(8070);


        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                rpcServer.stop();
                break;
            }

            for (String clientEndpoint : rpcServer.getClientEndpoints()) {
                INotification notification = rpcServer.getRpc(clientEndpoint, INotification.class);
                notification.notify(line);
            }
        }
    }
}

import java.util.Scanner;

public class NettyClient {

    private static String host = "127.0.0.1";
    private static int MAX_RETRY = 5;

    public static void main(String[] args) {
        RpcServiceRegistry.register(INotification.class, new INotification() {
            @Override
            public void notify(String message) {
                System.out.println("Notification:"+message);
            }
        });
        ICalculator calculator = RpcClientBuilder.createRpc(host, 8070, ICalculator.class);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("a=");
            int a = scanner.nextInt();
            System.out.print("b=");
            int b = scanner.nextInt();

            int c = calculator.div(a, b);
            System.out.printf("%d+%d=%d\n", a, b, c);
        }
    }
}
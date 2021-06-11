import java.util.Scanner;

public class NettyClient {

    private static String host = "127.0.0.1";
    private static int MAX_RETRY = 5;

    interface ICalculator {
        void add(int a, int b);
    }
    public static void main(String[] args) {
        ICalculator calculator = JsonRpcBuilder.createService(host, 8070, ICalculator.class);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();

            calculator.add(5, 7);
        }
    }
}
import java.util.Scanner;

public class NettyClient {

    private static String host = "127.0.0.1";
    private static int MAX_RETRY = 5;

    public static void main(String[] args) {
        ClientConnection connection = new ClientConnection();
        connection.connect(host, 8070, 30);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            connection.getChannel().writeAndFlush(line);
        }
    }
}
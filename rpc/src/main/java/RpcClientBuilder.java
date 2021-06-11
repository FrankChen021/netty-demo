import java.io.IOException;
import java.lang.reflect.Proxy;

public class RpcClientBuilder {

    static ConnectionManager connectionManager = new ConnectionManager();

    public static <T> T createRpc(String host, int port, Class<T> serviceInterface) {
        String endpoint = host + ":" + port;

        ClientConnection connection = connectionManager.getConnections().computeIfAbsent(endpoint, (key) -> {
            ClientConnection rpcConnection = new ClientConnection();
            rpcConnection.connect(host, port, ClientConnection.MAX_RETRY);
            rpcConnection.setIncomeMessageHandler((channel, message) -> {
                try {
                    RpcMessageReceiver.getInstance().onResponse(channel, message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return rpcConnection;
        });

        return (T) Proxy.newProxyInstance(RpcClientBuilder.class.getClassLoader(),
                                          new Class[]{serviceInterface},
                                          new RpcClientInvocationHandler(connection,
                                                                         RpcClientInvocationManager.getInstance()));
    }
}

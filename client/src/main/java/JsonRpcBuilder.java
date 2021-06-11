import java.lang.reflect.Proxy;

public class JsonRpcBuilder {

    static ConnectionManager connectionManager = new ConnectionManager();

    public static <T> T createService(String host, int port, Class<T> serviceInterface) {
        String endpoint = host + ":" + port;

        ClientConnection connection = connectionManager.getConnections().computeIfAbsent(endpoint, (key)->{
            ClientConnection rpcConnection = new ClientConnection();
            rpcConnection.connect(host, port, ClientConnection.MAX_RETRY);
            return rpcConnection;
        });

        return (T)Proxy.newProxyInstance(JsonRpcBuilder.class.getClassLoader(), new Class[]{serviceInterface}, new JsonRpcInvocationHandler(connection));
    }
}

import io.netty.channel.Channel;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientBuilder {

    private static Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    public static <T> T createRpc(String host, int port, Class<T> serviceInterface) {
        String endpoint = host + ":" + port;

        ClientConnection connection = connections.computeIfAbsent(endpoint, (key) -> {
            ClientConnection rpcConnection = new ClientConnection();
            rpcConnection.connect(host, port, ClientConnection.MAX_RETRY);
            return rpcConnection;
        });

        return (T) Proxy.newProxyInstance(RpcClientBuilder.class.getClassLoader(),
                                          new Class[]{serviceInterface},
                                          new RpcClientInvocationHandler(connection,
                                                                         RpcClientInvocationManager.getInstance()));
    }

    public static <T> T createRpc(Channel channel, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(RpcClientBuilder.class.getClassLoader(),
                                          new Class[]{serviceInterface},
                                          new RpcClientInvocationHandler(() -> channel,
                                                                         RpcClientInvocationManager.getInstance()));
    }
}

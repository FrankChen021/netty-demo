import io.netty.channel.Channel;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClientBuilder {

    private static final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends IService> T createRpc(String host, int port, Class<T> serviceInterface) {
        String remoteEndpoint = host + ":" + port;

        ClientConnection connection = connections.computeIfAbsent(remoteEndpoint, (key) -> {
            ClientConnection rpcConnection = new ClientConnection();
            rpcConnection.connect(host, port, ClientConnection.MAX_RETRY);
            return rpcConnection;
        });

        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                          new Class[]{serviceInterface},
                                          new RpcClientInvocationHandler(connection,
                                                                         RpcClientInvocationManager.getInstance()));
    }

    @SuppressWarnings("unchecked")
    public static <T extends IService> T createRpc(Channel channel, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                          new Class[]{serviceInterface},
                                          new RpcClientInvocationHandler(() -> channel,
                                                                         RpcClientInvocationManager.getInstance()));
    }
}

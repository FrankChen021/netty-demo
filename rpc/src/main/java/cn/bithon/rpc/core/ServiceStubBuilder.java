package cn.bithon.rpc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceStubBuilder {

    private static final Map<String, ClientConnection> connections = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends IService> T create(String host, int port, Class<T> serviceInterface) {
        String remoteEndpoint = host + ":" + port;

        ClientConnection connection = connections.computeIfAbsent(remoteEndpoint, (key) -> {
            ClientConnection serviceConnection = new ClientConnection();
            serviceConnection.connect(host, port, ClientConnection.MAX_RETRY);
            return serviceConnection;
        });

        return create(connection, serviceInterface);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IService> T create(IServiceChannelProvider channelProvider, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                          new Class[]{serviceInterface},
                                          new ServiceInvocationHandler(channelProvider,
                                                                       ServiceRequestManager.getInstance()));
    }


    static class ServiceInvocationHandler implements InvocationHandler {

        private final IServiceChannelProvider channelProvider;
        private final ServiceRequestManager requestManager;

        public ServiceInvocationHandler(IServiceChannelProvider channelProvider,
                                        ServiceRequestManager requestManager) {
            this.channelProvider = channelProvider;
            this.requestManager = requestManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return requestManager.invoke(channelProvider.getChannel(), method, args);
        }
    }
}

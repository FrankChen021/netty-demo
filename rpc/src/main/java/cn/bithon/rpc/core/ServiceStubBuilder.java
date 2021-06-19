package cn.bithon.rpc.core;

import cn.bithon.rpc.core.channel.ClientChannelProvider;
import cn.bithon.rpc.core.channel.IServiceChannelProvider;
import cn.bithon.rpc.core.exception.ServiceInvocationException;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceStubBuilder {

    @SuppressWarnings("unchecked")
    public static <T extends IService> T create(ClientChannelProvider channelProvider, Class<T> serviceInterface) {
        channelProvider.connect();
        return create(channelProvider, serviceInterface);
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
            Channel ch = channelProvider.getChannel();
            if (ch == null) {
                throw new ServiceInvocationException("Failed to invoke %s#%s due to channel is empty",
                                                     method.getDeclaringClass().getSimpleName(),
                                                     method.getName());
            }
            if (!ch.isActive()) {
                throw new ServiceInvocationException("Failed to invoke %s#%s due to channel is not active",
                                                     method.getDeclaringClass().getSimpleName(),
                                                     method.getName());
            }
            return requestManager.invoke(ch, method, args);
        }
    }
}

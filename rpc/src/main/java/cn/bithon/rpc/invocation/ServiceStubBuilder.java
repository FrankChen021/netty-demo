package cn.bithon.rpc.invocation;

import cn.bithon.rpc.IService;
import cn.bithon.rpc.channel.IServiceChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceStubBuilder {

    private static Method setDebugMethod;
    private static Method toStringMethod;
    private static Method setTimeoutMethod;
    private static Method toInvokerMethod;

    static {
        try {
            toStringMethod = Object.class.getMethod("toString");
            toInvokerMethod = IService.class.getMethod("toInvoker");
            setDebugMethod = IServiceInvoker.class.getMethod("debug", boolean.class);
            setTimeoutMethod = IServiceInvoker.class.getMethod("setTimeout", long.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IService> T create(IServiceChannel channelProvider, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                          new Class[]{serviceInterface, IServiceInvoker.class},
                                          new ServiceInvocationHandler(channelProvider,
                                                                       ServiceRequestManager.getInstance()));
    }

    static class ServiceInvocationHandler implements InvocationHandler {
        private final IServiceChannel channelProvider;
        private final ServiceRequestManager requestManager;
        private boolean debugEnabled;
        private long timeout = 5000;

        public ServiceInvocationHandler(IServiceChannel channelProvider,
                                        ServiceRequestManager requestManager) {
            this.channelProvider = channelProvider;
            this.requestManager = requestManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (toStringMethod.equals(method)) {
                return "ServiceInvocationHandler";
            }
            if (setDebugMethod.equals(method)) {
                debugEnabled = (boolean) args[0];
                return null;
            }
            if (setTimeoutMethod.equals(method)) {
                this.timeout = (long) args[0];
                return null;
            }
            if (toInvokerMethod.equals(method)) {
                return proxy;
            }

            return requestManager.invoke(channelProvider, debugEnabled, timeout, method, args);
        }
    }
}

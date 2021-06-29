package cn.bithon.rpc.invocation;

import cn.bithon.rpc.IService;
import cn.bithon.rpc.IServiceController;
import cn.bithon.rpc.channel.IChannelWriter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceStubFactory {

    private static Method setDebugMethod;
    private static Method toStringMethod;
    private static Method setTimeoutMethod;
    private static Method toControllerMethod;
    private static Method rstTimeoutMethod;

    static {
        try {
            toStringMethod = Object.class.getMethod("toString");
            toControllerMethod = IService.class.getMethod("toController");
            setDebugMethod = IServiceController.class.getMethod("debug", boolean.class);
            setTimeoutMethod = IServiceController.class.getMethod("setTimeout", long.class);
            rstTimeoutMethod = IServiceController.class.getMethod("rstTimeout");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends IService> T create(IChannelWriter channelWriter, Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
                                          new Class[]{serviceInterface, IServiceController.class},
                                          new ServiceInvocationHandler(channelWriter,
                                                                       ClientInvocationManager.getInstance()));
    }

    static class ServiceInvocationHandler implements InvocationHandler {
        private final IChannelWriter channelWriter;
        private final ClientInvocationManager clientInvocationManager;
        private boolean debugEnabled;
        private long timeout = 5000;

        public ServiceInvocationHandler(IChannelWriter channelWriter,
                                        ClientInvocationManager clientInvocationManager) {
            this.channelWriter = channelWriter;
            this.clientInvocationManager = clientInvocationManager;
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
            if (toControllerMethod.equals(method)) {
                return proxy;
            }
            if (rstTimeoutMethod.equals(method)) {
                this.timeout = 5000;
                return null;
            }
            return clientInvocationManager.invoke(channelWriter, debugEnabled, timeout, method, args);
        }
    }
}

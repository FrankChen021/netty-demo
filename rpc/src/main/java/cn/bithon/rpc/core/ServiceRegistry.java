package cn.bithon.rpc.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private static final Map<String, RpcServiceProvider> registry = new ConcurrentHashMap<>();

    public static <T extends IService> void register(Class<T> interfaceType, T impl) {
        // override methods are not supported
        for (Method method : interfaceType.getDeclaredMethods()) {
            registry.put(interfaceType.getSimpleName() + "#" + method.getName(), new RpcServiceProvider(method, impl));
        }
    }

    public static RpcServiceProvider findServiceProvider(String serviceName, String methodName) {
        return registry.get(serviceName + "#" + methodName);
    }

    public static class RpcServiceProvider {
        private final Method method;
        private final Object serviceImpl;
        private final boolean isReturnVoid;

        public RpcServiceProvider(Method method, Object serviceImpl) {
            this.method = method;
            this.serviceImpl = serviceImpl;
            isReturnVoid = method.getReturnType().equals(Void.TYPE);
        }

        public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(serviceImpl, args);
        }

        public boolean isReturnVoid() {
            return isReturnVoid;
        }

        public Parameter[] getParameters() {
            return method.getParameters();
        }
    }
}

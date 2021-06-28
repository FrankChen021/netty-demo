package cn.bithon.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

    private final Map<String, RegistryItem> registry = new ConcurrentHashMap<>();

    public <T extends IService> void addService(Class<T> serviceType, T serviceImpl) {
        // override methods are not supported
        for (Method method : serviceType.getDeclaredMethods()) {
            String qualifiedName = method.toString();
            RegistryItem item = registry.put(qualifiedName, new RegistryItem(method, serviceImpl));
            if (item != null) {
                log.error("{} is overwritten", item.method);
            }
        }
    }

    public RegistryItem findServiceProvider(CharSequence serviceName, CharSequence methodName) {
        return registry.get(methodName);
    }

    public static class ParameterType {
        private final Type rawType;
        private final Type messageType;

        public ParameterType(Type rawType, Type messageType) {
            this.rawType = rawType;
            this.messageType = messageType;
        }

        public Type getRawType() {
            return rawType;
        }

        public Type getMessageType() {
            return messageType;
        }
    }

    public static class RegistryItem {
        private final Method method;
        private final Object serviceImpl;
        private final boolean isOneway;
        private final Type[] parameterTypes;

        public RegistryItem(Method method, Object serviceImpl) {
            this.method = method;
            this.serviceImpl = serviceImpl;
            this.isOneway = method.getAnnotation(Oneway.class) != null;
            this.parameterTypes = method.getGenericParameterTypes();
        }

        public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(serviceImpl, args);
        }

        public boolean isOneway() {
            return isOneway;
        }

        public Type[] getParameterTypes() {
            return parameterTypes;
        }
    }
}

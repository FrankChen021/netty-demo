package cn.bithon.rpc;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceRegistry {

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
        private final Class<?> rawType;
        private final Class<?> messageType;

        public ParameterType(Class<?> rawType, Class<?> messageType) {
            this.rawType = rawType;
            this.messageType = messageType;
        }

        public Class<?> getRawType() {
            return rawType;
        }

        public Class<?> getMessageType() {
            return messageType;
        }
    }

    public static class RegistryItem {
        private final Method method;
        private final Object serviceImpl;
        private final boolean isOneway;
        private final ParameterType[] parameterTypes;

        public RegistryItem(Method method, Object serviceImpl) {
            this.method = method;
            this.serviceImpl = serviceImpl;
            this.isOneway = method.getAnnotation(Oneway.class) != null;
            this.parameterTypes = new ParameterType[method.getParameterCount()];

            Class<?>[] parameterRawTypes = method.getParameterTypes();
            for (int i = 0; i < parameterRawTypes.length; i++) {
                parameterTypes[i] = new ParameterType(parameterRawTypes[i],
                                                      parameterRawTypes[i]);
            }
        }

        public Object invoke(Object[] args) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(serviceImpl, args);
        }

        public boolean isOneway() {
            return isOneway;
        }

        public ParameterType[] getParameterTypes() {
            return parameterTypes;
        }
    }
}

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcRegistry {

    private static Map<String, RegistryItem> registry = new ConcurrentHashMap<>();

    public static <T> void register(Class<T> interfaceType, T impl) {
        // override methods are not supported
        for (Method method : interfaceType.getDeclaredMethods()) {
            registry.put(interfaceType.getSimpleName() + "#" + method.getName(), new RegistryItem(method, impl));
        }
    }

    public static RegistryItem findRpcMethod(String serviceName, String methodName) {
        return registry.get(serviceName + "#" + methodName);
    }

    @Getter
    @AllArgsConstructor
    public static class RegistryItem {
        private Method method;
        private Object impl;
    }
}

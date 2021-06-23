package cn.bithon.rpc.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ObjectBinarySerializer {

    interface Serializer {
        void serialize(Object obj, OutputStream os);
    }

    static Map<String, Serializer> primitiveTypes = new HashMap<>();

    static {
        primitiveTypes.put(Character.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Byte.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Short.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Integer.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Float.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Double.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(Long.class.getName(), (obj, os) -> {
        });
        primitiveTypes.put(String.class.getName(), (obj, os) -> {
        });
    }

    public void serialize(Object obj, OutputStream os) throws IOException {
        Serializer serializer = primitiveTypes.get(obj.getClass().getName());
        if (serializer != null) {
            serializer.serialize(obj, os);
            return;
        }
        if (obj instanceof Collection) {
            Collection<?> list = (Collection<?>) obj;
            os.write(list.size());
            for (Object listElement : list) {
                serialize(listElement, os);
            }
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            os.write(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object val = entry.getValue();
                serialize(key, os);
                serialize(val, os);
            }
        } else if (obj.getClass().isArray()) {
            Object[] objs = (Object[]) obj;
            os.write(objs.length);
            for (Object o : objs) {
                serialize(o, os);
            }
        } else {
            // protobuf structure
        }
    }
}

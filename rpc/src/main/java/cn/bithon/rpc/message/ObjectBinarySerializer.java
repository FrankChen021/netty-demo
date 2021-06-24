package cn.bithon.rpc.message;


import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectBinarySerializer {

    interface SerializerDispatcher {
        void serialize(Object obj, CodedOutputStream os) throws IOException;

        Object deserialize(CodedInputStream is) throws IOException;
    }

    Map<Class<?>, SerializerDispatcher> primitiveTypesSerializer = new HashMap<>();
    SerializerDispatcher defaultSerializer = new CompositeSerializer();

    public ObjectBinarySerializer() {
        primitiveTypesSerializer.put(boolean.class, new BooleanSerializer());
        primitiveTypesSerializer.put(Boolean.class, new BooleanSerializer());

        primitiveTypesSerializer.put(char.class, new CharSerializer());
        primitiveTypesSerializer.put(Character.class, new CharSerializer());

        primitiveTypesSerializer.put(byte.class, new ByteSerializer());
        primitiveTypesSerializer.put(Byte.class, new ByteSerializer());

        primitiveTypesSerializer.put(short.class, new ShortSerializer());
        primitiveTypesSerializer.put(Short.class, new ShortSerializer());
        primitiveTypesSerializer.put(int.class, new IntSerializer());
        primitiveTypesSerializer.put(Integer.class, new IntSerializer());

        primitiveTypesSerializer.put(float.class, new FloatSerializer());
        primitiveTypesSerializer.put(Float.class, new FloatSerializer());

        primitiveTypesSerializer.put(double.class, new DoubleSerializer());
        primitiveTypesSerializer.put(Double.class, new DoubleSerializer());

        primitiveTypesSerializer.put(long.class, new LongSerializer());
        primitiveTypesSerializer.put(Long.class, new LongSerializer());

        primitiveTypesSerializer.put(String.class, new StringSerializer());
    }

    public void serialize(MessageLite obj, CodedOutputStream os) throws IOException {
        obj.writeTo(os);
    }

    public void serialize(Collection<?> obj, CodedOutputStream os) throws IOException {
        os.writeInt32NoTag(obj.size());
        for (Object o : obj) {
            serialize(o, os);
        }
    }

    public void serialize(Map<?, ?> obj, CodedOutputStream os) throws IOException {
        os.writeInt32NoTag(obj.size());
        for (Map.Entry<?, ?> entry : obj.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            serialize(key, os);
            serialize(val, os);
        }
    }

    public void serialize(Object[] obj, CodedOutputStream os) throws IOException {
        os.writeInt32NoTag(obj.length);
        for (Object o : obj) {
            serialize(o, os);
        }
    }

    public void serialize(Object obj, CodedOutputStream os) throws IOException {
        primitiveTypesSerializer.getOrDefault(obj.getClass(), defaultSerializer).serialize(obj, os);
    }


    @SuppressWarnings("unchecked")
    public <T> T deserialize(CodedInputStream is, Class<T> clazz) throws IOException {
        SerializerDispatcher serializer = primitiveTypesSerializer.get(clazz);
        if (serializer != null) {
            return (T) serializer.deserialize(is);
        }
        if (clazz.isArray()) {
            return (T) deserializeArray(is, clazz);
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return (T) deserializeCollection(is, clazz);
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return (T) deserializeMap(is, clazz);
        }
        if (MessageLite.class.isAssignableFrom(clazz)) {
            try {
                Method method = clazz.getDeclaredMethod("parseFrom", CodedInputStream.class);
                return (T) method.invoke(null, is);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Unknown protocolBuf class to deserialize");
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Unable to deserialize object:" + e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("UnknowN class to deserialize:" + e.getMessage());
            }
        }
        throw new IllegalStateException("UnknowN class to deserialize");
    }

    private Object deserializeCollection(CodedInputStream is, Class<?> clazz) throws IOException {
        int size = is.readInt32();
        List<?> lists = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {

        }
        return lists;
    }

    private Object deserializeMap(CodedInputStream is, Class<?> clazz) throws IOException {
        int size = is.readInt32();

        Map<String, String> hashMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {

        }
        return hashMap;
    }

    private Object deserializeArray(CodedInputStream is, Class<?> clazz) throws IOException {
        int size = is.readInt32();

        for (int i = 0; i < size; i++) {

        }
        return null;
    }

    private class CompositeSerializer implements SerializerDispatcher {

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            if (obj instanceof Collection) {
                ObjectBinarySerializer.this.serialize((Collection<?>) obj, os);
            } else if (obj instanceof Map) {
                ObjectBinarySerializer.this.serialize((Map<?, ?>) obj, os);
            } else if (obj.getClass().isArray()) {
                ObjectBinarySerializer.this.serialize((Object[]) obj, os);
            } else if (obj instanceof MessageLite) {
                ObjectBinarySerializer.this.serialize((MessageLite) obj, os);
            } else {
                // unknown
                throw new IllegalStateException("unsupported type " + obj.getClass().getName());
            }
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return null;
        }
    }

    private static class ShortSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt32NoTag((short) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return (short)is.readInt32();
        }
    }

    private static class IntSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt32NoTag((int) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readInt32();
        }
    }

    private static class FloatSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeFloatNoTag((float) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readFloat();
        }
    }

    private static class DoubleSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeDoubleNoTag((double) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readDouble();
        }
    }

    private static class LongSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt64NoTag((long) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readInt64();
        }
    }

    private static class StringSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeStringNoTag((String) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readString();
        }
    }

    private static class ByteSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.write((byte) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readRawByte();
        }
    }

    private static class BooleanSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeBoolNoTag((boolean) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return is.readBool();
        }
    }

    private class CharSerializer implements SerializerDispatcher {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.write((byte) (char) obj);
        }

        @Override
        public Object deserialize(CodedInputStream is) throws IOException {
            return (char) is.readRawByte();
        }
    }
}

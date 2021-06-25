package cn.bithon.rpc.message;


import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BinarySerializer {

    public abstract static class TypeReference<T> implements Comparable<TypeReference<T>> {
        protected final Type _type;

        protected TypeReference() {
            Type superClass = this.getClass().getGenericSuperclass();
            if (superClass instanceof Class) {
                throw new IllegalArgumentException(
                    "Internal error: TypeReference constructed without actual type information");
            } else {
                this._type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            }
        }

        public Type getType() {
            return this._type;
        }

        @Override
        public int compareTo(TypeReference<T> o) {
            return 0;
        }

        public static Class<?> getRawClass(Type type) {
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            } else if (type instanceof ParameterizedType) {
                return getRawClass(((ParameterizedType) type).getRawType());
            } else if (type instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) type;
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length == 1) {
                    return getRawClass(upperBounds[0]);
                } else {
                    throw new IllegalStateException("TODO");
                }
            } else {
                throw new IllegalStateException("TODO");
            }
        }
    }

    interface IObjectSerializer {
        void serialize(Object obj, CodedOutputStream os) throws IOException;

        Object deserialize(Type type, CodedInputStream is) throws IOException;
    }

    public void serialize(MessageLite obj, CodedOutputStream os) throws IOException {
        ProtocolBufferSerializer.INSTANCE.serialize(obj, os);
    }

    public void serialize(Collection<?> obj, CodedOutputStream os) throws IOException {
        CollectionSerializer.INSTANCE.serialize(obj, os);
    }

    public void serialize(Map<?, ?> obj, CodedOutputStream os) throws IOException {
        MapSerializer.INSTANCE.serialize(obj, os);
    }

    public void serialize(Object[] obj, CodedOutputStream os) throws IOException {
        ArraySerializer.INSTANCE.serialize(obj, os);
    }

    public void serialize(Object obj, CodedOutputStream os) throws IOException {
        ObjectSerializer.INSTANCE.serialize(obj, os);
    }


    @SuppressWarnings("unchecked")
    public <T> T deserialize(CodedInputStream is, Class<T> clazz) throws IOException {
        return (T) ObjectSerializer.INSTANCE.deserialize(clazz, is);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(CodedInputStream is, TypeReference<T> typeReference) throws IOException {
        return (T) ObjectSerializer.INSTANCE.deserialize(typeReference.getType(), is);
    }

    private static class ObjectSerializer implements IObjectSerializer {

        public static final ObjectSerializer INSTANCE = new ObjectSerializer();

        private Map<Type, IObjectSerializer> serializers = new HashMap<>();

        public ObjectSerializer() {
            serializers.put(boolean.class, BooleanSerializer.INSTANCE);
            serializers.put(Boolean.class, BooleanSerializer.INSTANCE);

            serializers.put(char.class, CharSerializer.INSTANCE);
            serializers.put(Character.class, CharSerializer.INSTANCE);

            serializers.put(byte.class, ByteSerializer.INSTANCE);
            serializers.put(Byte.class, ByteSerializer.INSTANCE);

            serializers.put(short.class, ShortSerializer.INSTANCE);
            serializers.put(Short.class, ShortSerializer.INSTANCE);

            serializers.put(int.class, IntSerializer.INSTANCE);
            serializers.put(Integer.class, IntSerializer.INSTANCE);

            serializers.put(float.class, FloatSerializer.INSTANCE);
            serializers.put(Float.class, FloatSerializer.INSTANCE);

            serializers.put(double.class, DoubleSerializer.INSTANCE);
            serializers.put(Double.class, DoubleSerializer.INSTANCE);

            serializers.put(long.class, LongSerializer.INSTANCE);
            serializers.put(Long.class, LongSerializer.INSTANCE);

            serializers.put(String.class, new StringSerializer());
        }

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            IObjectSerializer serializer = serializers.get(obj.getClass());
            if (serializer != null) {
                serializer.serialize(obj, os);
                return;
            }
            if (obj instanceof Collection) {
                CollectionSerializer.INSTANCE.serialize(obj, os);
            } else if (obj instanceof Map) {
                MapSerializer.INSTANCE.serialize(obj, os);
            } else if (obj.getClass().isArray()) {
                ArraySerializer.INSTANCE.serialize(obj, os);
            } else if (obj instanceof MessageLite) {
                ProtocolBufferSerializer.INSTANCE.serialize(obj, os);
            } else {
                // unknown
                throw new IllegalStateException("unsupported type " + obj.getClass().getName());
            }
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            IObjectSerializer serializer = serializers.get(type);
            if (serializer != null) {
                return serializer.deserialize(type, is);
            }
            Class<?> clazz = TypeReference.getRawClass(type);

            if (clazz.isArray()) {
                return ArraySerializer.INSTANCE.deserialize(type, is);
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                return CollectionSerializer.INSTANCE.deserialize(type, is);
            }
            if (Map.class.isAssignableFrom(clazz)) {
                return MapSerializer.INSTANCE.deserialize(type, is);
            }
            if (MessageLite.class.isAssignableFrom(clazz)) {
                return ProtocolBufferSerializer.INSTANCE.deserialize(type, is);
            }

            throw new IllegalStateException("Unknown class to deserialize");
        }
    }

    private static class ShortSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new ShortSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt32NoTag((short) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return (short) is.readInt32();
        }
    }

    private static class IntSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new IntSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt32NoTag((int) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readInt32();
        }
    }

    private static class FloatSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new FloatSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeFloatNoTag((float) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readFloat();
        }
    }

    private static class DoubleSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new DoubleSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeDoubleNoTag((double) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readDouble();
        }
    }

    private static class LongSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new LongSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeInt64NoTag((long) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readInt64();
        }
    }

    private static class StringSerializer implements IObjectSerializer {
        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeStringNoTag((String) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readString();
        }
    }

    private static class ByteSerializer implements IObjectSerializer {
        private static final ByteSerializer INSTANCE = new ByteSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.write((byte) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readRawByte();
        }
    }

    private static class BooleanSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new BooleanSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.writeBoolNoTag((boolean) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return is.readBool();
        }
    }

    private static class CharSerializer implements IObjectSerializer {
        public static final IObjectSerializer INSTANCE = new CharSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            os.write((byte) (char) obj);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            return (char) is.readRawByte();
        }
    }

    private static class CollectionSerializer implements IObjectSerializer {
        public static final CollectionSerializer INSTANCE = new CollectionSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            Collection<?> collection = (Collection<?>) obj;
            os.writeInt32NoTag(collection.size());
            for (Object o : collection) {
                ObjectSerializer.INSTANCE.serialize(o, os);
            }
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            Type superType = type;
            if (type instanceof Class) {
                superType = ((Class<?>) type).getGenericSuperclass();
            }
            if (!(superType instanceof ParameterizedType)) {
                throw new IllegalStateException("Unsupport Map type to deserialize: " + type.toString());
            }
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            Type elementType = parameterizedType.getActualTypeArguments()[0];

            int size = is.readInt32();
            Collection<Object> lists = createCollectionInstance(type, size);
            for (int i = 0; i < size; i++) {
                lists.add(ObjectSerializer.INSTANCE.deserialize(elementType, is));
            }
            return lists;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Collection<Object> createCollectionInstance(Type type, int size) {
            Class<?> rawClass = TypeReference.getRawClass(type);
            Collection collection;
            if (rawClass == AbstractCollection.class || rawClass == Collection.class || rawClass == List.class) {
                collection = new ArrayList(size);
            } else if (rawClass.isAssignableFrom(HashSet.class)) {
                collection = new HashSet(size);
            } else if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
                collection = new LinkedHashSet(size);
            } else if (rawClass.isAssignableFrom(TreeSet.class)) {
                collection = new TreeSet();
            } else if (rawClass.isAssignableFrom(ArrayList.class)) {
                collection = new ArrayList(size);
            } else if (rawClass.isAssignableFrom(EnumSet.class)) {
                Type itemType;
                if (type instanceof ParameterizedType) {
                    itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
                } else {
                    itemType = Object.class;
                }
                collection = EnumSet.noneOf((Class<Enum>) itemType);
            } else if (rawClass.isAssignableFrom(Queue.class) || rawClass.isAssignableFrom(Deque.class)) {
                collection = new LinkedList();
            } else {
                try {
                    collection = (Collection) rawClass.newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("create instance error, class " + rawClass.getName());
                }
            }
            return collection;
        }
    }

    private static class MapSerializer implements IObjectSerializer {

        public static final MapSerializer INSTANCE = new MapSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            Map<?, ?> map = (Map<?, ?>) obj;
            os.writeInt32NoTag(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object val = entry.getValue();
                ObjectSerializer.INSTANCE.serialize(key, os);
                ObjectSerializer.INSTANCE.serialize(val, os);
            }
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            Type superType = type;
            if (type instanceof Class) {
                superType = ((Class<?>) type).getGenericSuperclass();
            }
            if (!(superType instanceof ParameterizedType)) {
                throw new IllegalStateException("Unsupport Map type to deserialize: " + type.toString());
            }
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            Type keyType = parameterizedType.getActualTypeArguments()[0];
            Type valueType = parameterizedType.getActualTypeArguments()[1];

            int size = is.readInt32();
            Map<Object, Object> map = createMapInstance(type, size);
            for (int i = 0; i < size; i++) {
                Object key = ObjectSerializer.INSTANCE.deserialize(keyType, is);
                Object val = ObjectSerializer.INSTANCE.deserialize(valueType, is);
                map.put(key, val);
            }
            return map;
        }

        private Map<Object, Object> createMapInstance(Type type, int size) {
            if (type == Properties.class) {
                return new Properties();
            }

            if (type == Hashtable.class) {
                return new Hashtable<>(size);
            }

            if (type == IdentityHashMap.class) {
                return new IdentityHashMap<>(size);
            }

            if (type == SortedMap.class || type == TreeMap.class) {
                return new TreeMap<>();
            }

            if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
                return new ConcurrentHashMap<>(size);
            }

            if (type == Map.class || type == HashMap.class) {
                return new HashMap<>(size);
            }

            if (type == LinkedHashMap.class) {
                return new LinkedHashMap<>(size);
            }

            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;

                Type rawType = parameterizedType.getRawType();
                if (EnumMap.class.equals(rawType)) {
                    Type[] actualArgs = parameterizedType.getActualTypeArguments();
                    //noinspection unchecked,rawtypes
                    return new EnumMap((Class<?>) actualArgs[0]);
                }

                return createMapInstance(rawType, size);
            }

            Class<?> clazz = (Class<?>) type;
            if (clazz.isInterface()) {
                throw new IllegalStateException("unsupport type " + type);
            }

            if ("java.util.Collections$UnmodifiableMap".equals(clazz.getName())) {
                return new HashMap<>(size);
            }

            try {
                //noinspection unchecked
                return (Map<Object, Object>) clazz.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("unsupport type " + type, e);
            }
        }
    }

    private static class ProtocolBufferSerializer implements IObjectSerializer {

        public static final ProtocolBufferSerializer INSTANCE = new ProtocolBufferSerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            ((MessageLite) obj).writeTo(os);
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            try {
                Method method = ((Class<?>) type).getDeclaredMethod("parseFrom", CodedInputStream.class);
                return method.invoke(null, is);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Unknown protocolBuf class to deserialize");
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Unable to deserialize object:" + e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("UnknowN class to deserialize:" + e.getMessage());
            }
        }
    }

    private static class ArraySerializer implements IObjectSerializer {
        public static final ArraySerializer INSTANCE = new ArraySerializer();

        @Override
        public void serialize(Object obj, CodedOutputStream os) throws IOException {
            Object[] arr = (Object[]) obj;
            os.writeInt32NoTag(arr.length);
            for (Object o : arr) {
                ObjectSerializer.INSTANCE.serialize(o, os);
            }
        }

        @Override
        public Object deserialize(Type type, CodedInputStream is) throws IOException {
            int size = is.readInt32();
            for (int i = 0; i < size; i++) {

            }
            return null;
        }
    }
}

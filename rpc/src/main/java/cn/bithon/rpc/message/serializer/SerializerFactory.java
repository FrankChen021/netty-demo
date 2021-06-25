package cn.bithon.rpc.message.serializer;

public class SerializerFactory {
    public static ISerializer getSerializer(int type) {
        if (type == JsonSerializer.INSTANCE.getType()) {
            return JsonSerializer.INSTANCE;
        }
        if (type == BinarySerializer.INSTANCE.getType()) {
            return BinarySerializer.INSTANCE;
        }
        throw new IllegalArgumentException("Unknown serializer: " + type);
    }
}

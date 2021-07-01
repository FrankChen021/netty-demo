package com.sbss.bithon.component.brpc.message.serializer;

public class SerializerFactory {
    public static ISerializer getSerializer(int type) {
        if (type == BinarySerializer.INSTANCE.getType()) {
            return BinarySerializer.INSTANCE;
        }
        throw new IllegalArgumentException("Unknown serializer: " + type);
    }
}

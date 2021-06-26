package cn.bithon.rpc.message.serializer;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.lang.reflect.Type;

public class BinarySerializer implements ISerializer {
    public static BinarySerializer INSTANCE = new BinarySerializer();
    private final ProtocolBufferSerializer serializer = new ProtocolBufferSerializer();

    @Override
    public int getType() {
        return 0x525;
    }

    @Override
    public void serialize(CodedOutputStream os, Object obj) throws IOException {
        serializer.serialize(obj, os);
    }

    @Override
    public Object deserialize(CodedInputStream is, Type type) throws IOException {
        return serializer.deserialize(is, type);
    }
}

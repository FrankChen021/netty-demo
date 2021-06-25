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
    public void serialize(ByteBuf buf, Object obj) throws IOException {
        serializer.serialize(obj, CodedOutputStream.newInstance(new ByteBufOutputStream(buf)));
    }

    @Override
    public Object deserialize(ByteBuf buf, Type type) throws IOException {
        return serializer.deserialize(CodedInputStream.newInstance(new ByteBufInputStream(buf)), type);
    }

    @Override
    public void serialize(ByteBuf buf, Object[] args) throws IOException {
        CodedOutputStream is = CodedOutputStream.newInstance(new ByteBufOutputStream(buf));
        is.writeInt32NoTag(args.length);
        for (Object arg : args) {
            serializer.serialize(arg, is);
        }
    }

    @Override
    public Object[] deserialize(ByteBuf buf, Type[] types) throws IOException {
        CodedInputStream is = CodedInputStream.newInstance(new ByteBufInputStream(buf));
        int size = is.readInt32();
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            args[i] = serializer.deserialize(is, types[i]);
        }
        return args;
    }
}

package cn.bithon.rpc.message.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Type;

public class JsonSerializer implements ISerializer {
    public static JsonSerializer INSTANCE = new JsonSerializer();

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public int getType() {
        return 0x629;
    }

    @Override
    public void serialize(ByteBuf buf, Object obj) throws IOException {
        if (obj == null) {
            buf.writeInt(0);
            return;
        }
        byte[] bytes = om.writeValueAsBytes(obj);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public Object deserialize(ByteBuf is, Type type) throws IOException {
        int size = is.readInt();
        if (size != 0) {
            byte[] bytes = new byte[size];
            is.readBytes(bytes);
            return om.readValue(bytes, om.constructType(type));
        }
        return null;
    }

    @Override
    public void serialize(ByteBuf buf, Object[] args) throws IOException {
        buf.writeInt(args.length);
        for(Object arg : args) {
            serialize(buf, arg);
        }
    }

    @Override
    public Object[] deserialize(ByteBuf buf, Type[] types) throws IOException {
        int len = buf.readInt();
        Object[] args = new Object[len];
        for(int i = 0; i < len; i++) {
            args[i] = deserialize(buf, types[i]);
        }
        return args;
    }
}

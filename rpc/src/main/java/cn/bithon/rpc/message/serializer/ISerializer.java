package cn.bithon.rpc.message.serializer;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Type;

public interface ISerializer {
    int getType();
    void serialize(ByteBuf os, Object obj) throws IOException;
    Object deserialize(ByteBuf is, Type type) throws IOException;

    void serialize(ByteBuf buf, Object[] args) throws IOException;
    Object[] deserialize(ByteBuf buf, Type[] types) throws IOException;
}

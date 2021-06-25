package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessage;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

abstract public class ServiceMessageOut extends ServiceMessage {

    abstract public void encode(ByteBuf out) throws IOException;

    protected void writeString(CharSequence val, ByteBuf out) {
        if (val == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(val.length());
        out.writeCharSequence(val, StandardCharsets.UTF_8);
    }

    protected void writeBytes(byte[] bytes, ByteBuf out) {
        if (bytes == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}

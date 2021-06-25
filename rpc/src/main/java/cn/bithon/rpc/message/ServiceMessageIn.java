package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

abstract public class ServiceMessageIn extends ServiceMessage {

    abstract public ServiceMessage decode(ByteBuf in);

    protected CharSequence readString(ByteBuf in) {
        int len = in.readInt();
        if (len == 0) {
            return null;
        }
        return in.readCharSequence(len, StandardCharsets.UTF_8);
    }

    protected byte[] readBytes(ByteBuf in) {
        int len = in.readInt();
        if (len > 0) {
            byte[] bytes = new byte[len];
            in.readBytes(bytes);
            return bytes;
        } else {
            return null;
        }
    }
}

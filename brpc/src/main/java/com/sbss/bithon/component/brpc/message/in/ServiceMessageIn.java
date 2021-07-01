package com.sbss.bithon.component.brpc.message.in;

import com.google.protobuf.CodedInputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessage;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class ServiceMessageIn extends ServiceMessage {

    public abstract ServiceMessage decode(CodedInputStream in) throws IOException;

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

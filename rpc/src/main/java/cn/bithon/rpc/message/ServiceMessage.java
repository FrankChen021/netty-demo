package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public abstract class ServiceMessage {
    protected long transactionId;

    /**
     * {@link ServiceMessageType}
     */
    abstract public int getMessageType();

    public long getTransactionId() {
        return transactionId;
    }

    abstract public void encode(ByteBuf out);

    abstract public ServiceMessage decode(ByteBuf in);

    protected CharSequence readString(ByteBuf in) {
        int len = in.readInt();
        if (len == 0) {
            return null;
        }
        return in.readCharSequence(len, StandardCharsets.UTF_8);
    }

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

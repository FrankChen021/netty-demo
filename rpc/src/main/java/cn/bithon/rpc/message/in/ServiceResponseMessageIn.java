package cn.bithon.rpc.message.in;

import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Type;

public class ServiceResponseMessageIn extends ServiceMessageIn {
    private long serverResponseAt;
    private ByteBuf returning;
    private CharSequence exception;

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();

        this.serverResponseAt = in.readLong();
        this.exception = readString(in);

        boolean hasReturning = in.readByte() == 1;
        if (hasReturning) {
            this.returning = in;
        }
        return this;
    }

    public long getServerResponseAt() {
        return serverResponseAt;
    }

    public Object getReturning(Type type) throws IOException {
        if (returning != null) {
            int serializer = this.returning.readInt();
            return SerializerFactory.getSerializer(serializer)
                                    .deserialize(this.returning, type);
        }
        return null;
    }

    public CharSequence getException() {
        return exception;
    }
}

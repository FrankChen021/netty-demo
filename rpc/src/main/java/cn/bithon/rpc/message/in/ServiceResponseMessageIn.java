package cn.bithon.rpc.message.in;

import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Type;

public class ServiceResponseMessageIn extends ServiceMessageIn {
    private long serverResponseAt;
    private byte[] returning;
    private CharSequence exception;

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();

        this.serverResponseAt = in.readLong();
        this.returning = readBytes(in);
        this.exception = readString(in);
        return this;
    }

    public long getServerResponseAt() {
        return serverResponseAt;
    }

    public Object getReturning(Type type) throws IOException {
        if (returning != null) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(this.returning, om.getTypeFactory().constructType(type));
        }
        return null;
    }

    public CharSequence getException() {
        return exception;
    }
}

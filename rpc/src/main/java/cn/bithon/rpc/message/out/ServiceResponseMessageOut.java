package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.BinarySerializer;
import cn.bithon.rpc.message.serializer.ISerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

public class ServiceResponseMessageOut extends ServiceMessageOut {
    private long serverResponseAt;
    private Object returning;
    private CharSequence exception;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(this.getMessageType());
        out.writeLong(this.getTransactionId());

        out.writeLong(serverResponseAt);
        writeString(this.exception, out);

        if (this.returning == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);

            ISerializer serializer = BinarySerializer.INSTANCE;
            out.writeInt(serializer.getType());
            serializer.serialize(out, this.returning);
        }
    }

    public void setException(CharSequence exception) {
        this.exception = exception;
    }

    static public class Builder {
        ServiceResponseMessageOut response = new ServiceResponseMessageOut();

        public Builder serverResponseAt(long currentTimeMillis) {
            response.serverResponseAt = currentTimeMillis;
            return this;
        }

        public Builder txId(long txId) {
            response.transactionId = txId;
            return this;
        }

        public Builder exception(String exception) {
            response.exception = exception;
            return this;
        }

        public ServiceResponseMessageOut build() {
            return response;
        }

        public Builder returning(Object ret) {
            response.returning = ret;
            return this;
        }
    }
}

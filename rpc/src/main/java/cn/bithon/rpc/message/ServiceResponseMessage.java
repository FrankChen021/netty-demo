package cn.bithon.rpc.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

public class ServiceResponseMessage extends ServiceMessage {
    private long serverResponseAt;
    private byte[] returning;
    private CharSequence exception;

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeInt(this.getMessageType());
        out.writeLong(this.getTransactionId());

        out.writeLong(serverResponseAt);
        writeBytes(this.returning, out);
        writeString(this.exception, out);
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

    public void setServerResponseAt(long serverResponseAt) {
        this.serverResponseAt = serverResponseAt;
    }

    public byte[] getReturning() {
        return returning;
    }

    public void setReturning(byte[] returning) {
        this.returning = returning;
    }

    public CharSequence getException() {
        return exception;
    }

    public void setException(CharSequence exception) {
        this.exception = exception;
    }

    public static Builder builder() {
        return new Builder();
    }

    static public class Builder {
        ServiceResponseMessage response = new ServiceResponseMessage();

        public Builder serverResponseAt(long currentTimeMillis) {
            response.serverResponseAt = currentTimeMillis;
            return this;
        }

        public Builder transactionId(long txId) {
            response.transactionId = txId;
            return this;
        }

        public Builder exception(String exception) {
            response.exception = exception;
            return this;
        }

        public ServiceResponseMessage build() {
            return response;
        }

        @SneakyThrows
        public Builder returning(Object ret) {
            response.returning = new ObjectMapper().writeValueAsBytes(ret);
            return this;
        }
    }
}

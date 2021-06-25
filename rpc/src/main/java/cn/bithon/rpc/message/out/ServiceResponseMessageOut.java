package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

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
        writeBytes(this.returning == null ? null : new ObjectMapper().writeValueAsBytes(this.returning), out);
        writeString(this.exception, out);
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

        public Builder transactionId(long txId) {
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

        @SneakyThrows
        public Builder returning(Object ret) {
            response.returning = ret;
            return this;
        }
    }
}

package cn.bithon.rpc.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;

public class ServiceRequestMessage extends ServiceMessage {

    private CharSequence serviceName;
    private CharSequence methodName;

    /**
     * args
     */
    private byte[] args;

    @Override
    public Long getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeLong(this.getMessageType());
        out.writeLong(this.getTransactionId());

        writeString(this.serviceName, out);
        writeString(this.methodName, out);

        writeBytes(this.args, out);
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();
        this.serviceName = readString(in);
        this.methodName = readString(in);
        this.args = readBytes(in);

        return this;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ServiceRequestMessage request = new ServiceRequestMessage();

        public Builder serviceName(String serviceName) {
            request.serviceName = serviceName;
            return this;
        }

        public Builder methodName(String methodName) {
            request.methodName = methodName;
            return this;
        }

        public Builder transactionId(long txId) {
            request.transactionId = txId;
            return this;
        }

        @SneakyThrows
        public Builder args(Object[] args) {
            request.args = new ObjectMapper().writeValueAsBytes(args);
            return this;
        }

        public ServiceRequestMessage build() {
            return request;
        }
    }

    public CharSequence getServiceName() {
        return serviceName;
    }

    public CharSequence getMethodName() {
        return methodName;
    }

    public byte[] getArgs() {
        return args;
    }
}

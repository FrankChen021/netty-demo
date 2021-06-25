package cn.bithon.rpc.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class ServiceRequestMessageOut extends ServiceMessageOut {

    private CharSequence serviceName;
    private CharSequence methodName;

    public CharSequence getServiceName() {
        return serviceName;
    }

    public CharSequence getMethodName() {
        return methodName;
    }

    /**
     * args
     */
    private Object[] args;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public void encode(ByteBuf out) throws IOException {
        out.writeInt(this.getMessageType());
        out.writeLong(this.getTransactionId());

        writeString(this.serviceName, out);
        writeString(this.methodName, out);

        writeBytes(new ObjectMapper().writeValueAsBytes(this.args), out);
    }

    public static class Builder {
        private final ServiceRequestMessageOut request = new ServiceRequestMessageOut();

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

        public Builder args(Object[] args) {
            request.args = args;
            return this;
        }

        public ServiceRequestMessageOut build() {
            return request;
        }
    }
}

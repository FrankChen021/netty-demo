package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.BinarySerializer;
import cn.bithon.rpc.message.serializer.ISerializer;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;

public class ServiceRequestMessageOut extends ServiceMessageOut {

    private String serviceName;
    private String methodName;

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
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
    public void encode(CodedOutputStream out) throws IOException {
        out.writeInt32NoTag(this.getMessageType());
        out.writeInt64NoTag(this.getTransactionId());

        out.writeStringNoTag(this.serviceName);
        out.writeStringNoTag(this.methodName);

        ISerializer serializer = BinarySerializer.INSTANCE;
        out.writeInt32NoTag(serializer.getType());
        out.writeInt32NoTag(this.args.length);
        for (Object arg : this.args) {
            serializer.serialize(out, arg);
        }
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

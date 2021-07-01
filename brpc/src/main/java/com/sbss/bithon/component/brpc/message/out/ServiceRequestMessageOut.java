package com.sbss.bithon.component.brpc.message.out;

import com.google.protobuf.CodedOutputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessageType;
import com.sbss.bithon.component.brpc.message.serializer.Serializer;

import java.io.IOException;

public class ServiceRequestMessageOut extends ServiceMessageOut {

    private boolean isOneway;
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
        return isOneway ? ServiceMessageType.CLIENT_REQUEST_ONEWAY : ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public void encode(CodedOutputStream out) throws IOException {
        out.writeInt32NoTag(this.getMessageType());
        out.writeInt64NoTag(this.getTransactionId());

        out.writeStringNoTag(this.serviceName);
        out.writeStringNoTag(this.methodName);

        Serializer serializer = getSerializer();
        out.writeInt32NoTag(serializer.getType());
        out.writeInt32NoTag(this.args.length);
        for (Object arg : this.args) {
            serializer.serialize(out, arg);
        }
    }

    public boolean isOneway() {
        return isOneway;
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

        public Builder isOneway(boolean isOneway) {
            request.isOneway = isOneway;
            return this;
        }

        public ServiceRequestMessageOut build() {
            return request;
        }

        public Builder serializer(Serializer serializer) {
            request.setSerializer(serializer);
            return this;
        }
    }
}

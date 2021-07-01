package com.sbss.bithon.component.brpc.message.out;

import com.google.protobuf.CodedOutputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessageType;
import com.sbss.bithon.component.brpc.message.serializer.BinarySerializer;
import com.sbss.bithon.component.brpc.message.serializer.ISerializer;

import java.io.IOException;

public class ServiceResponseMessageOut extends ServiceMessageOut {
    private long serverResponseAt;
    private Object returning;
    private String exception;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public void encode(CodedOutputStream out) throws IOException {
        out.writeInt32NoTag(this.getMessageType());
        out.writeInt64NoTag(this.getTransactionId());

        out.writeInt64NoTag(serverResponseAt);

        if (this.exception == null) {
            out.writeRawByte(0);
        } else {
            out.writeRawByte(1);
            out.writeStringNoTag(this.exception);
        }

        if (this.returning == null) {
            out.writeRawByte(0);
        } else {
            out.writeRawByte(1);

            ISerializer serializer = BinarySerializer.INSTANCE;
            out.writeInt32NoTag(serializer.getType());
            serializer.serialize(out, this.returning);
        }
    }

    public static class Builder {
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

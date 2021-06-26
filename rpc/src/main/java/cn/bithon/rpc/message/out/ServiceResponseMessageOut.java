package cn.bithon.rpc.message.out;

import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.BinarySerializer;
import cn.bithon.rpc.message.serializer.ISerializer;
import com.google.protobuf.CodedOutputStream;

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
        out.writeStringNoTag(this.exception == null ? "" : this.exception);
        if (this.returning == null) {
            out.writeRawByte(0);
        } else {
            out.writeRawByte(1);

            ISerializer serializer = BinarySerializer.INSTANCE;
            out.writeInt32NoTag(serializer.getType());
            serializer.serialize(out, this.returning);
        }
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

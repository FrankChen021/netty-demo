package cn.bithon.rpc.message.in;

import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.ISerializer;
import cn.bithon.rpc.message.serializer.SerializerFactory;
import com.google.protobuf.CodedInputStream;

import java.io.IOException;
import java.lang.reflect.Type;

public class ServiceRequestMessageIn extends ServiceMessageIn {

    private CharSequence serviceName;
    private CharSequence methodName;

    /**
     * args
     */
    private CodedInputStream args;

    @Override
    public int getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public ServiceMessage decode(CodedInputStream in) throws IOException {
        this.transactionId = in.readInt64();
        this.serviceName = in.readString();
        this.methodName = in.readString();
        this.args = in;
        return this;
    }

    public CharSequence getServiceName() {
        return serviceName;
    }

    public CharSequence getMethodName() {
        return methodName;
    }

    public Object[] getArgs(Type[] parameterTypes) throws BadRequestException, IOException {
        int serializerType = this.args.readInt32();

        int argLength = this.args.readInt32();
        if (argLength != parameterTypes.length) {
            throw new BadRequestException(String.format("Argument size not match. Expected %d, but given %d",
                                                        parameterTypes.length,
                                                        argLength));
        }

        ISerializer serializer = SerializerFactory.getSerializer(serializerType);
        Object[] inputArgs = new Object[argLength];
        for (int i = 0; i < argLength; i++) {
            try {
                inputArgs[i] = serializer.deserialize(this.args, parameterTypes[i]);
            } catch (IOException e) {
                throw new BadRequestException("Bad args for %s#%s: %s",
                                              serviceName,
                                              methodName,
                                              e.getMessage());
            }
        }
        return inputArgs;
    }
}

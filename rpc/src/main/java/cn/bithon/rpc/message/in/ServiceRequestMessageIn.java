package cn.bithon.rpc.message.in;

import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.serializer.ISerializer;
import cn.bithon.rpc.message.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.Type;

public class ServiceRequestMessageIn extends ServiceMessageIn {

    private CharSequence serviceName;
    private CharSequence methodName;

    /**
     * args
     */
    private ByteBuf args;
    private int argLength;
    private int serializerType;

    @Override
    public int getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();
        this.serviceName = readString(in);
        this.methodName = readString(in);
        this.serializerType = in.readInt();
        this.args = in;
        return this;
    }

    public CharSequence getServiceName() {
        return serviceName;
    }

    public CharSequence getMethodName() {
        return methodName;
    }

    //public byte[] getArgs() {
    //   return args;
    //}

    public Object[] getArgs(Type[] parameterTypes) throws BadRequestException {

//        if (this.argLength != parameterTypes.length) {
//            throw new BadRequestException(String.format("Argument size not match. Expected %d, but given %d",
//                                                        parameterTypes.length,
//                                                        this.argLength));
//        }

        try {
            ISerializer serializer = SerializerFactory.getSerializer(this.serializerType);
            return serializer.deserialize(this.args, parameterTypes);
        } catch (IOException e) {
            throw new BadRequestException("Bad args for %s#%s: %s",
                                          serviceName,
                                          methodName,
                                          e.getMessage());
        }
    }
}

package cn.bithon.rpc.message.in;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class ServiceRequestMessageIn extends ServiceMessageIn {

    private CharSequence serviceName;
    private CharSequence methodName;

    /**
     * args
     */
    private byte[] args;
    private int argLength;

    @Override
    public int getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();
        this.serviceName = readString(in);
        this.methodName = readString(in);
        this.argLength = in.readInt();
        this.args = readBytes(in);
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

    public Object[] getArgs(ServiceRegistry.ParameterType[] parameterTypes)
        throws BadRequestException {

        Object[] inputArgs = new Object[parameterTypes.length];
        if (parameterTypes.length <= 0) {
            return inputArgs;
        }

        ObjectMapper om = new ObjectMapper();
        JsonNode argsNode;
        try {
            argsNode = om.readTree(this.args);
        } catch (IOException e) {
            throw new BadRequestException("Can't deserialize args");
        }
        if (argsNode == null || argsNode.isNull()) {
            throw new BadRequestException("args is null");
        }

        if (!argsNode.isArray()) {
            throw new BadRequestException("Bad args type");
        }

        ArrayNode argsArrayNode = (ArrayNode) argsNode;
        if (argsArrayNode.size() != parameterTypes.length) {
            throw new BadRequestException(
                "Bad args for %s#%s, expected %d parameters, but provided %d parameters",
                serviceName,
                methodName,
                parameterTypes.length,
                argsArrayNode.size());
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            JsonNode inputArgNode = argsArrayNode.get(i);
            if (inputArgNode != null && !inputArgNode.isNull()) {
                try {
                    inputArgs[i] = om.convertValue(inputArgNode, parameterTypes[i].getMessageType());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Bad args for %s#%s at %d: %s",
                                                  serviceName,
                                                  methodName,
                                                  i,
                                                  e.getMessage());
                }
            }
        }
        return inputArgs;
    }
}

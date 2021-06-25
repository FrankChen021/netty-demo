package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;

public class ServiceRequestMessageIn extends ServiceMessageIn {

    private CharSequence serviceName;
    private CharSequence methodName;

    /**
     * args
     */
    private byte[] args;

    @Override
    public int getMessageType() {
        return ServiceMessageType.CLIENT_REQUEST;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();
        this.serviceName = readString(in);
        this.methodName = readString(in);
        this.args = readBytes(in);

        return this;
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

package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;

public class ServiceResponseMessageIn extends ServiceMessageIn {
    private long serverResponseAt;
    private byte[] returning;
    private CharSequence exception;

    @Override
    public int getMessageType() {
        return ServiceMessageType.SERVER_RESPONSE;
    }

    @Override
    public ServiceMessage decode(ByteBuf in) {
        this.transactionId = in.readLong();

        this.serverResponseAt = in.readLong();
        this.returning = readBytes(in);
        this.exception = readString(in);
        return this;
    }

    public long getServerResponseAt() {
        return serverResponseAt;
    }

    public void setServerResponseAt(long serverResponseAt) {
        this.serverResponseAt = serverResponseAt;
    }

    public byte[] getReturning() {
        return returning;
    }

    public void setReturning(byte[] returning) {
        this.returning = returning;
    }

    public CharSequence getException() {
        return exception;
    }

    public void setException(CharSequence exception) {
        this.exception = exception;
    }

}

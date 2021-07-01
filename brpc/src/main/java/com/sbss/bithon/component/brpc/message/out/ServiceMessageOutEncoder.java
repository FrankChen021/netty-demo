package com.sbss.bithon.component.brpc.message.out;

import com.google.protobuf.CodedOutputStream;
import com.sbss.bithon.component.brpc.invocation.ClientInvocationManager;
import com.sbss.bithon.component.brpc.message.ServiceMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceMessageOutEncoder extends MessageToByteEncoder<ServiceMessageOut> {
    private static final Logger log = LoggerFactory.getLogger(ServiceMessageOutEncoder.class);

    static class ServiceMessageEncodingException extends EncoderException {
        final ServiceMessageOut out;

        public ServiceMessageEncodingException(ServiceMessageOut out, Throwable cause) {
            super(cause);
            this.out = out;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ServiceMessageOut msg, ByteBuf out) {
        try {
            CodedOutputStream os = CodedOutputStream.newInstance(new ByteBufOutputStream(out));
            msg.encode(os);
            os.flush();
        } catch (Exception e) {
            throw new ServiceMessageEncodingException(msg, e);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                return;
            }

            // Handle encoding exception
            Throwable cause = future.cause();
            if (cause instanceof ServiceMessageEncodingException) {
                ServiceMessageOut out = ((ServiceMessageEncodingException) cause).out;
                if (out.getMessageType() == ServiceMessageType.CLIENT_REQUEST) {
                    ClientInvocationManager.getInstance()
                                           .onClientException(((ServiceMessageEncodingException) cause).out.getTransactionId(),
                                                              cause.getCause());
                    return;
                }
            }

                log.error("Exception when encoding out message", cause);
        }));
    }
}

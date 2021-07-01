package com.sbss.bithon.component.brpc.message.in;

import com.google.protobuf.CodedInputStream;
import com.sbss.bithon.component.brpc.message.ServiceMessageType;
import com.sbss.bithon.component.brpc.message.UnknownMessageException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class ServiceMessageInDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws IOException {
        CodedInputStream is = CodedInputStream.newInstance(new ByteBufInputStream(in));

        int messageType = is.readInt32();
        if (messageType == ServiceMessageType.CLIENT_REQUEST || messageType == ServiceMessageType.CLIENT_REQUEST_ONEWAY) {
            out.add(new ServiceRequestMessageIn().decode(is));
        } else if (messageType == ServiceMessageType.SERVER_RESPONSE) {
            out.add(new ServiceResponseMessageIn().decode(is));
        } else {
            throw new UnknownMessageException(messageType);
        }
    }
}

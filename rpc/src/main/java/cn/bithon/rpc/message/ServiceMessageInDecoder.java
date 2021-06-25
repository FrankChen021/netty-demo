package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ServiceMessageInDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() >= 4) {
            int messageType = in.readInt();
            if (messageType == ServiceMessageType.CLIENT_REQUEST) {
                out.add(new ServiceRequestMessageIn().decode(in));
            } else if (messageType == ServiceMessageType.SERVER_RESPONSE) {
                out.add(new ServiceResponseMessageIn().decode(in));
            } else {
                throw new RuntimeException("Unknown message type: " + messageType);
            }
        }
    }
}

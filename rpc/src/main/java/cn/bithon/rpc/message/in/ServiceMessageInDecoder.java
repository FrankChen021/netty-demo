package cn.bithon.rpc.message.in;

import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.UnknownMessageException;
import com.google.protobuf.CodedInputStream;
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
        if (messageType == ServiceMessageType.CLIENT_REQUEST) {
            out.add(new ServiceRequestMessageIn().decode(is));
        } else if (messageType == ServiceMessageType.SERVER_RESPONSE) {
            out.add(new ServiceResponseMessageIn().decode(is));
        } else {
            throw new UnknownMessageException(messageType);
        }
    }
}

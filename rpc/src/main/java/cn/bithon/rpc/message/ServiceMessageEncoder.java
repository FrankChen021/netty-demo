package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ServiceMessageEncoder extends MessageToByteEncoder<ServiceMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ServiceMessage msg, ByteBuf out) {
        msg.encode(out);
    }
}

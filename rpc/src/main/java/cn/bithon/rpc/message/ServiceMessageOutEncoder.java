package cn.bithon.rpc.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServiceMessageOutEncoder extends MessageToByteEncoder<ServiceMessageOut> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ServiceMessageOut msg, ByteBuf out) {
        try {
            msg.encode(out);
        } catch (IOException e) {
            log.error("Exception when encoding out message", e);
        }
    }
}

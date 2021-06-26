package cn.bithon.rpc.message.out;

import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServiceMessageOutEncoder extends MessageToByteEncoder<ServiceMessageOut> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ServiceMessageOut msg, ByteBuf out) {
        try {
            CodedOutputStream os = CodedOutputStream.newInstance(new ByteBufOutputStream(out));
            msg.encode(os);
            os.flush();
        } catch (IOException e) {
            log.error("Exception when encoding out message", e);
        }
    }
}

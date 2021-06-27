package cn.bithon.rpc.message.out;

import com.google.protobuf.CodedOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ServiceMessageOutEncoder extends MessageToByteEncoder<ServiceMessageOut> {
    private final static Logger log = LoggerFactory.getLogger(ServiceMessageOutEncoder.class);

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

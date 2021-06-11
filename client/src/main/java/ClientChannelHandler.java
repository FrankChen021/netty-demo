import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private ClientConnection connection;

    public ClientChannelHandler(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        connection.setChannel(ctx.channel());

        ctx.channel().writeAndFlush("s");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info(new Date() + ": 客户端读到数据 -> " + msg.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        connection.setChannel(null);

        log.warn("Connection closed, trying re-connect...");
        this.connection.doConnect(this.connection.getMaxRetry());

        super.channelInactive(ctx);
    }
}
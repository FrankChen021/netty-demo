import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.Charset;
import java.util.Date;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    //channel对象数组
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 1. 获取数据
        //ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date() + ": 服务端读到数据 -> " + msg);
        System.out.println(new Date() + ": 服务端写出数据");
        // 2. 写数据
        //ByteBuf out = getByteBuf(ctx);

        ctx.channel().writeAndFlush("From Server HHH");
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        byte[] bytes = "我是发送给客户端的数据：请重启冰箱!".getBytes(Charset.forName("utf-8"));
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(bytes);
        return buffer;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channelGroup.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    }
}
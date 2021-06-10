import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    public static Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();

        System.out.println(new Date() + ": 客户端写出数据");

        // 1. 获取数据
        ByteBuf buffer = getByteBuf(ctx);

        // 2. 写数据
        ctx.channel().writeAndFlush(buffer);
    }

    /**
     * 数据解析
     */
    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        // 1. 获取二进制抽象 ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();
        Random random = new Random();
        double value = random.nextDouble() * 14 + 8;
        String temp = "获取室内温度：" + value;

        // 2. 准备数据，指定字符串的字符集为 utf-8
        byte[] bytes = temp.getBytes(StandardCharsets.UTF_8);

        // 3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(new Date() + ": 客户端读到数据 -> " + msg.toString());
    }

}
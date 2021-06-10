import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Scanner;

public class NettyServer {

    private static NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private static NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
            .group(bossGroup, workerGroup)

            // 指定Channel
            .channel(NioServerSocketChannel.class)

            //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
            .option(ChannelOption.SO_BACKLOG, 1024)

            //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
            .childOption(ChannelOption.SO_KEEPALIVE, true)

            //将小的数据包包装成更大的帧进行传送，提高网络的负载
            .childOption(ChannelOption.TCP_NODELAY, true)

            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast(new NettyServerHandler());
                }
            });

        serverBootstrap.bind(8070);

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String line = scanner.nextLine();
            NettyServerHandler.channelGroup.writeAndFlush(line, (channel)->{
                return true;
            } );
        }
    }

//    @PreDestroy
//    public void destory() throws InterruptedException {
//        bossGroup.shutdownGracefully().sync();
//        workerGroup.shutdownGracefully().sync();
//    }

}
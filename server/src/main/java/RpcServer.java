import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class RpcServer {

    private final NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    public <T> RpcServer addService(Class<T> interfaceClass, T impl) {
        RpcServiceRegistry.register(interfaceClass, impl);
        return this;
    }

    public RpcServer start(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
            .option(ChannelOption.SO_BACKLOG, 1024)
            //设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
            .childOption(ChannelOption.SO_KEEPALIVE, false)
            //将小的数据包包装成更大的帧进行传送，提高网络的负载
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    ch.pipeline().addLast(new RpcServerChannelHandler());
                }
            });
        serverBootstrap.bind(port);

        return this;
    }

    public void stop() {
        try {
            bossGroup.shutdownGracefully().sync();
        } catch (InterruptedException ignored) {
        }
        try {
            workerGroup.shutdownGracefully().sync();
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {
        RpcServer rpcServer = new RpcServer()
            .addService(ICalculator.class, new ICalculator() {
                @Override
                public int add(int a, int b) {
                    return a + b;
                }

                @Override
                public int div(int a, int b) {
                    return a / b;
                }
            }).start(8070);


        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if ("stop".equals(line)) {
                rpcServer.stop();
                break;
            }

            for (Channel channel : RpcServerChannelHandler.channelGroup) {
                INotification notification = RpcClientBuilder.createRpc(channel, INotification.class);
                notification.notify(line);
            }
        }
    }
}
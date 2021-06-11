import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientConnection {

    public static final int MAX_RETRY = 30;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    private final Bootstrap bootstrap;
    private Channel channel;
    private String host;
    private int port;
    private int maxRetry;

    public ClientConnection() {
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("decoder", new StringDecoder());
                ch.pipeline().addLast("encoder", new StringEncoder());
                ch.pipeline().addLast(new ChannelHandler());
            }
        });
    }

    public void connect(String host, int port, int maxRetry) {
        this.host = host;
        this.port = port;
        this.maxRetry = maxRetry;
        doConnect(maxRetry);
    }

    private void doConnect(int maxRetry) {
        bootstrap.connect(host, port).addListener((ChannelFuture channelFuture) -> {
            if (!channelFuture.isSuccess()) {
                final EventLoop loop = channelFuture.channel().eventLoop();
                if ( maxRetry == 0 ) {
                    log.warn("无法连接{}:{}，已达到最大重试次数{}", this.host, this.port, this.maxRetry);
                    return;
                }
                log.warn("无法连接{}:{}，{}秒后重试。剩余重试次数:{}", this.host, this.port, 1, maxRetry);
                loop.schedule(() -> {
                    doConnect(maxRetry - 1);
                }, 1L, TimeUnit.SECONDS);
            } else {
                log.info("成功连接{}:{}", this.host, this.port);
            }
        });
    }

    public class ChannelHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ClientConnection.this.setChannel(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            log.info(new Date() + ": 客户端读到数据 -> " + msg.toString());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ClientConnection.this.setChannel(null);

            log.warn("Connection closed, trying re-connect...");
            ClientConnection.this.doConnect(ClientConnection.this.maxRetry);

            super.channelInactive(ctx);
        }
    }
}

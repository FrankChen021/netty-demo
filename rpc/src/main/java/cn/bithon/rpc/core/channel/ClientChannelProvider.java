package cn.bithon.rpc.core.channel;

import cn.bithon.rpc.core.endpoint.IEndPointProvider;
import cn.bithon.rpc.core.endpoint.SingleEndPointProvider;
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

import java.util.concurrent.TimeUnit;

/**
 * Should only be used at the client side
 */
@Slf4j
public class ClientChannelProvider implements IServiceChannelProvider {

    public static final int MAX_RETRY = 30;
    private final Bootstrap bootstrap;
    private final IEndPointProvider endPointProvider;
    private final int maxRetry;
    private NioEventLoopGroup bossGroup;
    private Channel channel;
    private boolean isConnecting = false;

    public ClientChannelProvider(String host, int port) {
        this(host, port, MAX_RETRY);
    }

    public ClientChannelProvider(String host, int port, int maxRetry) {
        this(new SingleEndPointProvider(host, port), maxRetry);
    }

    public ClientChannelProvider(IEndPointProvider endPointProvider, int maxRetry) {
        this.endPointProvider = endPointProvider;
        this.maxRetry = maxRetry;

        bossGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) {
                         ch.pipeline().addLast("decoder", new StringDecoder());
                         ch.pipeline().addLast("encoder", new StringEncoder());
                         ch.pipeline().addLast(new ClientChannelManager());
                         ch.pipeline().addLast(new ServiceChannelReader());
                     }
                 });
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public void connect() {
        if (bossGroup == null) {
            throw new IllegalStateException("client channel has been shutdown");
        }
        if (channel == null || !isConnecting) {
            doConnect(maxRetry);
        }
    }

    public void shutdown() {
        try {
            bossGroup.shutdownGracefully().sync();
        } catch (InterruptedException ignored) {
        }
        bossGroup = null;
    }

    private void doConnect(int maxRetry) {
        isConnecting = true;

        IEndPointProvider.Endpoint endpoint = endPointProvider.getEndpoint();
        bootstrap.connect(endpoint.getHost(), endpoint.getPort()).addListener((ChannelFuture channelFuture) -> {
            if (!channelFuture.isSuccess()) {
                final EventLoop loop = channelFuture.channel().eventLoop();
                if (maxRetry == 0) {
                    log.warn("无法连接{}:{}，已达到最大重试次数{}", endpoint.getHost(), endpoint.getPort(), this.maxRetry);
                    isConnecting = false;
                    return;
                }
                log.warn("无法连接{}:{}，{}秒后重试。剩余重试次数:{}", endpoint.getHost(), endpoint.getPort(), 1, maxRetry);
                loop.schedule(() -> doConnect(maxRetry - 1),
                              1L,
                              TimeUnit.SECONDS);
            } else {
                isConnecting = false;
                log.info("成功连接{}:{}", endpoint.getHost(), endpoint.getPort());
            }
        });
    }

    class ClientChannelManager extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ClientChannelProvider.this.channel = ctx.channel();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ClientChannelProvider.this.channel = null;

            log.warn("Connection closed, trying re-connect...");
            ClientChannelProvider.this.doConnect(ClientChannelProvider.this.maxRetry);

            super.channelInactive(ctx);
        }
    }
}

package cn.bithon.rpc.channel;

import cn.bithon.rpc.IService;
import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.endpoint.IEndPointProvider;
import cn.bithon.rpc.endpoint.SingleEndPointProvider;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.invocation.ServiceMessageHandler;
import cn.bithon.rpc.invocation.ServiceStubFactory;
import cn.bithon.rpc.message.ServiceMessageDecoder;
import cn.bithon.rpc.message.ServiceMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Should only be used at the client side
 */
@Slf4j
public class ClientChannel implements IChannelWriter, IChannelConnectable, Closeable {

    //
    // channel
    //
    public static final int MAX_RETRY = 30;
    private final Bootstrap bootstrap;
    private NioEventLoopGroup bossGroup;
    private final AtomicReference<Channel> channel = new AtomicReference<>();

    private final IEndPointProvider endPointProvider;
    private Duration retryInterval;
    private int maxRetry;

    private final ServiceRegistry serviceRegistry = new ServiceRegistry();

    public ClientChannel(String host, int port) {
        this(new SingleEndPointProvider(host, port));
    }

    public ClientChannel(IEndPointProvider endPointProvider) {
        this.endPointProvider = endPointProvider;
        this.maxRetry = MAX_RETRY;
        this.retryInterval = Duration.ofMillis(100);

        bossGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(bossGroup)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) {
                         ChannelPipeline pipeline = ch.pipeline();
                         pipeline.addLast("frameDecoder",
                                          new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                         pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                         pipeline.addLast("decoder", new ServiceMessageDecoder());
                         pipeline.addLast("encoder", new ServiceMessageEncoder());
                         pipeline.addLast(new ClientChannelManager());
                         pipeline.addLast(new ServiceMessageHandler(serviceRegistry));
                     }
                 });
    }

    @Override
    public Channel getChannel() {
        return channel.get();
    }

    @Override
    public void writeAndFlush(Object obj) {
        Channel ch = channel.get();
        if (ch == null) {
            throw new ServiceInvocationException("Client channel is closed");
        }
        ch.writeAndFlush(obj);
    }

    @Override
    synchronized public void connect() {
        if (bossGroup == null) {
            throw new IllegalStateException("client channel has been shutdown");
        }
        if (channel.get() == null) {
            doConnect(maxRetry);
        }
    }

    public ClientChannel configureRetry(int maxRetry, Duration interval) {
        this.maxRetry = maxRetry;
        this.retryInterval = interval;
        return this;
    }

    @Override
    public void close() {
        try {
            this.bossGroup.shutdownGracefully().sync();
        } catch (InterruptedException ignored) {
        }
        this.bossGroup = null;
        this.channel.getAndSet(null);
    }

    private void doConnect(int maxRetry) {
        for (int i = 0; i < maxRetry; i++) {
            IEndPointProvider.Endpoint endpoint = endPointProvider.getEndpoint();
            try {
                Future<?> connectFuture = bootstrap.connect(endpoint.getHost(), endpoint.getPort());
                connectFuture.await(200, TimeUnit.MILLISECONDS);
                if (connectFuture.isSuccess()) {
                    log.info("Successfully connected to server({}:{})", endpoint.getHost(), endpoint.getPort());
                    return;
                }
                int leftCount = maxRetry - i - 1;
                if (leftCount > 0) {
                    log.warn("Unable to connect to server({}:{})。Left retry count:{}",
                             endpoint.getHost(),
                             endpoint.getPort(),
                             maxRetry - i - 1);
                    Thread.sleep(retryInterval.toMillis());
                }
            } catch (InterruptedException e) {
                throw new ServiceInvocationException("Unable to connect to server, interrupted");
            }
        }
        throw new ServiceInvocationException("Unable to connect to server");
    }

    class ClientChannelManager extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ClientChannel.this.channel.getAndSet(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ClientChannel.this.channel.getAndSet(null);
            super.channelInactive(ctx);
        }
    }

    public <T extends IService> ClientChannel bindService(Class<T> serviceType, T serviceImpl) {
        serviceRegistry.addService(serviceType, serviceImpl);
        return this;
    }

    public <T extends IService> T getRemoteService(Class<T> serviceType) {
        return ServiceStubFactory.create(this, serviceType);
    }
}

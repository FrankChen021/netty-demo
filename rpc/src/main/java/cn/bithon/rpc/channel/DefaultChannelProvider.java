package cn.bithon.rpc.channel;

import io.netty.channel.Channel;

public class DefaultChannelProvider implements IServiceChannel {

    private final Channel channel;

    public DefaultChannelProvider(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public void writeAndFlush(Object obj) {
        channel.writeAndFlush(obj);
    }
}

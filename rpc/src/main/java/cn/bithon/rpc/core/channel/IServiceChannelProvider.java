package cn.bithon.rpc.core.channel;

import io.netty.channel.Channel;

public interface IServiceChannelProvider {

    /**
     * get underlying channel object
     */
    Channel getChannel();

    void writeAndFlush(Object obj);

    void debug(boolean on);
}

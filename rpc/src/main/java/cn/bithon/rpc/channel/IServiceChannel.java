package cn.bithon.rpc.channel;

import io.netty.channel.Channel;

public interface IServiceChannel {

    /**
     * get underlying channel object
     */
    Channel getChannel();

    void writeAndFlush(Object obj);
}

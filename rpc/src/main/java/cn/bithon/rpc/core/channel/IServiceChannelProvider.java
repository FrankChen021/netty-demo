package cn.bithon.rpc.core.channel;

import io.netty.channel.Channel;

public interface IServiceChannelProvider {

    Channel getChannel();
}

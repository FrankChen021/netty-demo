import io.netty.channel.Channel;

public interface IRpcChannelProvider {

    Channel getChannel();
}

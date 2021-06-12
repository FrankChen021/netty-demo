import io.netty.channel.Channel;

public interface IServiceChannelProvider {

    Channel getChannel();
}

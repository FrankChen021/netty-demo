import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * interface Calculator {
 * int add(int a, int b);
 * }
 */
public class RpcClientInvocationHandler implements InvocationHandler {

    private final IRpcChannelProvider channelProvider;
    private final RpcClientInvocationManager rpcClientInvocationManager;

    public RpcClientInvocationHandler(IRpcChannelProvider channelProvider,
                                      RpcClientInvocationManager rpcClientInvocationManager) {
        this.channelProvider = channelProvider;
        this.rpcClientInvocationManager = rpcClientInvocationManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return rpcClientInvocationManager.sendClientRequest(channelProvider.getChannel(), method, args);
    }
}

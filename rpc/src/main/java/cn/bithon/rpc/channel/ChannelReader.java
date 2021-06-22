package cn.bithon.rpc.channel;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.invocation.ServiceInvocationDispatcher;
import cn.bithon.rpc.invocation.ServiceRequestManager;
import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.ServiceRequestMessage;
import cn.bithon.rpc.message.ServiceResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ChannelReader extends ChannelInboundHandlerAdapter {

    private final ServiceInvocationDispatcher serviceInvocationDispatcher;
    private boolean channelDebugEnabled;

    public ChannelReader(ServiceRegistry serviceRegistry) {
        this.serviceInvocationDispatcher = new ServiceInvocationDispatcher(serviceRegistry);
    }

    public ChannelReader(ServiceInvocationDispatcher serviceInvocationDispatcher) {
        this.serviceInvocationDispatcher = serviceInvocationDispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ServiceMessage)) {
            return;
        }

        ServiceMessage message = (ServiceMessage) msg;
        if (message.getMessageType() == ServiceMessageType.CLIENT_REQUEST) {
            ServiceRequestMessage request = (ServiceRequestMessage) message;
            if (channelDebugEnabled) {
                log.info("receiving request, txId={}, service={}#{}",
                         request.getTransactionId(),
                         request.getServiceName(),
                         request.getMethodName());
            }

            serviceInvocationDispatcher.dispatch(ctx.channel(), (ServiceRequestMessage) message);
        } else if (message.getMessageType() == ServiceMessageType.SERVER_RESPONSE) {
            if (channelDebugEnabled) {
                log.info("receiving response, txId={}", message.getTransactionId());
            }

            ServiceRequestManager.getInstance().onResponse((ServiceResponseMessage) message);
        }
    }

    public boolean isChannelDebugEnabled() {
        return channelDebugEnabled;
    }

    public void setChannelDebugEnabled(boolean channelDebugEnabled) {
        this.channelDebugEnabled = channelDebugEnabled;
    }
}

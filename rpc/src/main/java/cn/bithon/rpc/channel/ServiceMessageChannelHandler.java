package cn.bithon.rpc.channel;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.invocation.ClientInvocationManager;
import cn.bithon.rpc.invocation.IServiceInvocationExecutor;
import cn.bithon.rpc.invocation.ServiceInvocationRunnable;
import cn.bithon.rpc.message.ServiceMessage;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.ServiceRequestMessage;
import cn.bithon.rpc.message.ServiceResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ServiceMessageChannelHandler extends ChannelInboundHandlerAdapter {

    private final IServiceInvocationExecutor invoker;
    private final ServiceRegistry serviceRegistry;
    private final ObjectMapper om = new ObjectMapper();
    private boolean channelDebugEnabled;

    public ServiceMessageChannelHandler(ServiceRegistry serviceRegistry) {
        this(serviceRegistry, ServiceInvocationRunnable::run);
    }

    public ServiceMessageChannelHandler(ServiceRegistry serviceRegistry, IServiceInvocationExecutor dispatcher) {
        this.serviceRegistry = serviceRegistry;
        this.invoker = dispatcher;
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

            invoker.invoke(new ServiceInvocationRunnable(om,
                                                         serviceRegistry,
                                                         ctx.channel(),
                                                         (ServiceRequestMessage) message));
        } else if (message.getMessageType() == ServiceMessageType.SERVER_RESPONSE) {
            if (channelDebugEnabled) {
                log.info("receiving response, txId={}", message.getTransactionId());
            }

            ClientInvocationManager.getInstance().onResponse((ServiceResponseMessage) message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception occurred when processing message", cause);
    }

    public boolean isChannelDebugEnabled() {
        return channelDebugEnabled;
    }

    public void setChannelDebugEnabled(boolean channelDebugEnabled) {
        this.channelDebugEnabled = channelDebugEnabled;
    }
}

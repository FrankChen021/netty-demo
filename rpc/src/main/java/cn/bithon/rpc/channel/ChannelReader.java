package cn.bithon.rpc.channel;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.invocation.ServiceInvocationDispatcher;
import cn.bithon.rpc.invocation.ServiceRequestManager;
import cn.bithon.rpc.message.ServiceMessageType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@ChannelHandler.Sharable
public class ChannelReader extends ChannelInboundHandlerAdapter {

    private final ServiceInvocationDispatcher serviceInvocationDispatcher;
    private boolean channelDebugEnabled;
    private final ObjectMapper om = new JsonMapper();

    public ChannelReader(ServiceRegistry serviceRegistry) {
        this.serviceInvocationDispatcher = new ServiceInvocationDispatcher(serviceRegistry);
    }

    public ChannelReader(ServiceInvocationDispatcher serviceInvocationDispatcher) {
        this.serviceInvocationDispatcher = serviceInvocationDispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (channelDebugEnabled) {
            log.info("receiving: {}", msg);
        }

        try {
            JsonNode messageJsonNode = om.readTree(msg.toString());
            JsonNode messageTypeNode = messageJsonNode.get("messageType");
            if (messageTypeNode == null) {
                return;
            }

            long messageType = messageTypeNode.asLong();
            if (messageType == ServiceMessageType.CLIENT_REQUEST) {
                serviceInvocationDispatcher.dispatch(ctx.channel(), messageJsonNode);
            } else if (messageType == ServiceMessageType.SERVER_RESPONSE) {
                ServiceRequestManager.getInstance().onResponse(messageJsonNode);
            }
        } catch (IOException e) {
        }
    }

    public boolean isChannelDebugEnabled() {
        return channelDebugEnabled;
    }

    public void setChannelDebugEnabled(boolean channelDebugEnabled) {
        this.channelDebugEnabled = channelDebugEnabled;
    }
}

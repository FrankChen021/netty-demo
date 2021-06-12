import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ServiceChannelReader extends ChannelInboundHandlerAdapter {

    private final ObjectMapper om = new JsonMapper();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("receiving: {}", msg);

        try {
            JsonNode messageJsonNode = om.readTree(msg.toString());
            JsonNode messageTypeNode = messageJsonNode.get("messageType");
            if (messageTypeNode == null) {
                return;
            }

            long messageType = messageTypeNode.asLong();
            if (messageType == ServiceMessageType.CLIENT_REQUEST) {
                ServiceInvocationManager.getInstance().invoke(ctx.channel(), messageJsonNode);
            } else if (messageType == ServiceMessageType.SERVER_RESPONSE) {
                ServiceRequestManager.getInstance().onResponse(messageJsonNode);
            }
        } catch (IOException e) {

        }
    }


}

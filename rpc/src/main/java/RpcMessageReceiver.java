import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class RpcMessageReceiver {

    private static RpcMessageReceiver INSTANCE = new RpcMessageReceiver();
    private final ObjectMapper om = new JsonMapper();

    public static RpcMessageReceiver getInstance() {
        return INSTANCE;
    }

    public void onResponse(Channel channel, Object message) throws IOException {
        log.info("receiving: {}", message);

        JsonNode messageNode = om.readTree(message.toString());

        JsonNode messageTypeNode = messageNode.get("messageType");
        if (messageTypeNode == null) {
            return;
        }

        long messageType = messageTypeNode.asLong();
        if (messageType == RpcMessageType.CLIENT_REQUEST) {
            RpcServerInvocationManager.getInstance().invoke(channel, messageNode);
        } else if (messageType == RpcMessageType.SERVER_RESPONSE) {
            RpcClientInvocationManager.getInstance().onResponse(messageNode);
        }
    }
}

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RpcClientInvocationManager {

    private static RpcClientInvocationManager INSTANCE = new RpcClientInvocationManager();
    private final AtomicLong transactionId = new AtomicLong();
    private final ObjectMapper om = new JsonMapper();
    private Map<Long, RpcResponse> pendingRequests = new ConcurrentHashMap<>();

    public static RpcClientInvocationManager getInstance() {
        return INSTANCE;
    }

    public void onResponse(JsonNode response) throws IOException {
        JsonNode transactionId = response.get("transactionId");
        if (transactionId == null) {
            return;
        }
        long txId = transactionId.asLong();

        RpcResponse rpcResponse = pendingRequests.get(txId);
        if (rpcResponse == null) {
            return;
        }

        JsonNode responseNode = response.get("response");
        rpcResponse.response = om.convertValue(responseNode, rpcResponse.responseType);
        synchronized (rpcResponse) {
            rpcResponse.notify();
        }
    }

    public Object sendClientRequest(Channel channel, Object obj, Method method, Object[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.serviceName = method.getDeclaringClass().getSimpleName();
        rpcRequest.methodName = method.getName();
        rpcRequest.transactionId = transactionId.incrementAndGet();
        rpcRequest.messageType = RpcMessageType.CLIENT_REQUEST;
        rpcRequest.args = args;

        log.info("sending client request:{}", rpcRequest);

        Class returnType = method.getReturnType();
        RpcResponse rpcResponse = null;
        if (returnType != null) {
            rpcResponse = new RpcResponse();
            rpcResponse.responseType = returnType;
            this.pendingRequests.put(rpcRequest.transactionId, rpcResponse);
        }
        try {
            channel.writeAndFlush(om.writeValueAsString(rpcRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (rpcResponse != null) {
            try {
                synchronized (rpcResponse) {
                    rpcResponse.wait(5000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return rpcResponse.response;
        }
        return null;
    }

    @Data
    public static class RpcResponse {
        long messageType;
        long transactionId;
        long requestAt;
        long responseAt;
        Class responseType;
        Object response;
    }

    @Data
    static class RpcRequest {
        private String serviceName;
        private String methodName;
        private Long transactionId;
        private Long messageType;
        private Object[] args;
    }
}

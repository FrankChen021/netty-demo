import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.channel.Channel;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerInvocationManager {

    private static RpcServerInvocationManager INSTANCE = new RpcServerInvocationManager();
    private final Executor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1,
                                                             50,
                                                             60L,
                                                             TimeUnit.SECONDS,
                                                             new SynchronousQueue<Runnable>());
    ObjectMapper om = new ObjectMapper();

    public static RpcServerInvocationManager getInstance() {
        return INSTANCE;
    }

    //        @Data
//        static class RpcRequest {
//            private String serviceName;
//            private String methodName;
//            private Long transactionId;
//            private Object[] args;
//        }

    public void invoke(Channel channel, JsonNode messageNode) {
        executor.execute(() -> {
            JsonNode serviceNameNode = messageNode.get("serviceName");
            JsonNode methodNameNode = messageNode.get("methodName");
            JsonNode txIdNode = messageNode.get("transactionId");
            ArrayNode args = (ArrayNode) messageNode.get("args");

            String serviceName = serviceNameNode.asText();
            String methodName = methodNameNode.asText();
            RpcRegistry.RegistryItem rpcRegistry = RpcRegistry.findRpcMethod(serviceName, methodName);


            Parameter[] parameters = rpcRegistry.getMethod().getParameters();
            Object[] inputArgs = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                JsonNode inputArgNode = args.get(i);
                if (inputArgNode != null) {
                    inputArgs[i] = om.convertValue(inputArgNode, parameters[i].getType());
                }
            }

            Object ret = null;
            try {
                ret = rpcRegistry.getMethod().invoke(rpcRegistry.getImpl(), inputArgs);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (rpcRegistry.getMethod().getReturnType() != null) {

                RpcResponse rpcResponse = new RpcResponse();
                rpcResponse.response = ret;
                rpcResponse.responseAt = System.currentTimeMillis();
                rpcResponse.transactionId = txIdNode.asLong();
                rpcResponse.messageType = RpcMessageType.SERVER_RESPONSE;
                try {
                    channel.writeAndFlush(om.writeValueAsString(rpcResponse));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Data
    public static class RpcResponse {
        long messageType;
        long transactionId;
        long responseAt;
        Object response;
    }
}

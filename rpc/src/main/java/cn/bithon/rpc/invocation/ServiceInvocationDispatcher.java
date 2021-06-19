package cn.bithon.rpc.invocation;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.message.ServiceException;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.ServiceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceInvocationDispatcher {

    private final ServiceRegistry serviceRegistry;

    private final Executor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1,
                                                             50,
                                                             0L, TimeUnit.MILLISECONDS,
                                                             new LinkedBlockingQueue<>(4096),
                                                             new RejectHandler());
    private final ObjectMapper om = new ObjectMapper();

    public ServiceInvocationDispatcher(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void dispatch(Channel channel, JsonNode messageNode) {
        executor.execute(new Invoker(om, channel, messageNode, serviceRegistry));
    }

    static class RejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Invoker invoker = (Invoker) r;

            JsonNode txIdNode = invoker.messageNode.get("transactionId");
            if (txIdNode != null && !txIdNode.isNull()) {
                long txId = txIdNode.asLong();
                sendResponse(invoker.channel, invoker.om, ServiceResponse.builder()
                                                                         .messageType(ServiceMessageType.SERVER_RESPONSE)
                                                                         .serverResponseAt(System.currentTimeMillis())
                                                                         .transactionId(txId)
                                                                         .exception(new ServiceException(
                                                                             "Server has no enough resources to process the request."))
                                                                         .build());
            }
        }
    }

    static void sendResponse(Channel channel, ObjectMapper om, ServiceResponse serviceResponse) {
        try {
            channel.writeAndFlush(om.writeValueAsBytes(serviceResponse));
        } catch (IOException e) {
            log.error(String.format("Exception sending RPC response(%s)", serviceResponse), e);
        }
    }

    @AllArgsConstructor
    public static class Invoker implements Runnable {
        private final ObjectMapper om;
        private final Channel channel;
        private final JsonNode messageNode;
        private final ServiceRegistry serviceRegistry;

        @Override
        public void run() {

            Long txId = null;

            try {
                JsonNode serviceNameNode = messageNode.get("serviceName");
                if (serviceNameNode == null || serviceNameNode.isNull()) {
                    throw new BadRequestException("serviceName is null");
                }

                JsonNode methodNameNode = messageNode.get("methodName");
                if (methodNameNode == null || methodNameNode.isNull()) {
                    throw new BadRequestException("methodName is null");
                }

                JsonNode txIdNode = messageNode.get("transactionId");
                if (txIdNode == null || txIdNode.isNull()) {
                    throw new BadRequestException("transactionId is null");
                }
                txId = txIdNode.asLong();

                String serviceName = serviceNameNode.asText();
                String methodName = methodNameNode.asText();
                ServiceRegistry.RpcServiceProvider serviceProvider = serviceRegistry.findServiceProvider(
                    serviceName,
                    methodName);
                if (serviceProvider == null) {
                    throw new BadRequestException("Can't find service provider %s#%s", serviceName, methodName);
                }

                Object[] inputArgs = parseArgs(serviceName, methodName, serviceProvider.getParameterTypes());

                Object ret;
                try {
                    ret = serviceProvider.invoke(inputArgs);
                } catch (IllegalAccessException e) {
                    throw new ServiceInvocationException("Service[%s#%s] exception: %s",
                                                         serviceName,
                                                         methodName,
                                                         e.getMessage());
                } catch (InvocationTargetException e) {
                    throw new ServiceInvocationException("Service[%s#%s] invocation exception: %s",
                                                         serviceName,
                                                         methodName,
                                                         e.getTargetException().toString());
                }

                if (!serviceProvider.isReturnVoid()) {
                    sendResponse(ServiceResponse.builder()
                                                .messageType(ServiceMessageType.SERVER_RESPONSE)
                                                .serverResponseAt(System.currentTimeMillis())
                                                .transactionId(txId)
                                                .returning(ret)
                                                .build());
                }
            } catch (BadRequestException e) {
                if (txId != null) {
                    sendResponse(ServiceResponse.builder()
                                                .messageType(ServiceMessageType.SERVER_RESPONSE)
                                                .serverResponseAt(System.currentTimeMillis())
                                                .transactionId(txId)
                                                .exception(new ServiceException(e.getMessage()))
                                                .build());
                }
            } catch (ServiceInvocationException e) {
                sendResponse(ServiceResponse.builder()
                                            .messageType(ServiceMessageType.SERVER_RESPONSE)
                                            .serverResponseAt(System.currentTimeMillis())
                                            .transactionId(txId)
                                            .exception(new ServiceException(e.getMessage()))
                                            .build());
            }
        }

        private void sendResponse(ServiceResponse serviceResponse) {
            ServiceInvocationDispatcher.sendResponse(channel, om, serviceResponse);
        }

        private Object[] parseArgs(String serviceName,
                                   String methodName,
                                   ServiceRegistry.ParameterType[] parameterTypes)
            throws BadRequestException {

            Object[] inputArgs = new Object[parameterTypes.length];
            if (parameterTypes.length <= 0) {
                return inputArgs;
            }

            JsonNode argsNode = messageNode.get("args");
            if (argsNode == null || argsNode.isNull()) {
                throw new BadRequestException("args is null");
            }

            if (!argsNode.isArray()) {
                throw new BadRequestException("Bad args type");
            }

            ArrayNode argsArrayNode = (ArrayNode) argsNode;
            if (argsArrayNode.size() != parameterTypes.length) {
                throw new BadRequestException(
                    "Bad args for %s#%s, expected %d parameters, but provided %d parameters",
                    serviceName,
                    methodName,
                    parameterTypes.length,
                    argsArrayNode.size());
            }

            for (int i = 0; i < parameterTypes.length; i++) {
                JsonNode inputArgNode = argsArrayNode.get(i);
                if (inputArgNode != null && !inputArgNode.isNull()) {
                    try {
                        inputArgs[i] = om.convertValue(inputArgNode, parameterTypes[i].getMessageType());
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Bad args for %s#%s at %d: %s",
                                                      serviceName,
                                                      methodName,
                                                      i,
                                                      e.getMessage());
                    }
                }
            }
            return inputArgs;
        }
    }
}

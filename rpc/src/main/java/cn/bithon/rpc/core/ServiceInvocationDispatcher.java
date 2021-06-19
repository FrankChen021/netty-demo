package cn.bithon.rpc.core;

import cn.bithon.rpc.core.exception.BadRequestException;
import cn.bithon.rpc.core.exception.ServiceInvocationException;
import cn.bithon.rpc.core.message.ServiceException;
import cn.bithon.rpc.core.message.ServiceMessageType;
import cn.bithon.rpc.core.message.ServiceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceInvocationDispatcher {

    private static final ServiceInvocationDispatcher INSTANCE = new ServiceInvocationDispatcher();

    public static ServiceInvocationDispatcher getInstance() {
        return INSTANCE;
    }

    private final Executor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1,
                                                             50,
                                                             60L,
                                                             TimeUnit.SECONDS,
                                                             new SynchronousQueue<>());
    private final ObjectMapper om = new ObjectMapper();

    public void dispatch(Channel channel, JsonNode messageNode) {
        executor.execute(new Invoker(om, channel, messageNode));
    }

    @AllArgsConstructor
    public static class Invoker implements Runnable {
        private final ObjectMapper om;
        private final Channel channel;
        private final JsonNode messageNode;

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
                ServiceRegistry.RpcServiceProvider serviceProvider = ServiceRegistry.findServiceProvider(
                    serviceName,
                    methodName);
                if (serviceProvider == null) {
                    throw new BadRequestException("Can't find service provider %s#%s", serviceName, methodName);
                }

                Object[] inputArgs = parseArgs(serviceName, methodName, serviceProvider.getParameters());

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
            try {
                channel.writeAndFlush(om.writeValueAsString(serviceResponse));
            } catch (IOException e) {
                log.error(String.format("Exception sending RPC response(%s)", serviceResponse), e);
            }
        }

        private Object[] parseArgs(String serviceName, String methodName, Parameter[] parameters)
            throws BadRequestException {

            Object[] inputArgs = new Object[parameters.length];
            if (parameters.length <= 0) {
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
            if (argsArrayNode.size() != parameters.length) {
                throw new BadRequestException(
                    "Bad args for %s#%s, expected %d parameters, but provided %d parameters",
                    serviceName,
                    methodName,
                    parameters.length,
                    argsArrayNode.size());
            }

            for (int i = 0; i < parameters.length; i++) {
                JsonNode inputArgNode = argsArrayNode.get(i);
                if (inputArgNode != null && !inputArgNode.isNull()) {
                    try {
                        inputArgs[i] = om.convertValue(inputArgNode, parameters[i].getType());
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

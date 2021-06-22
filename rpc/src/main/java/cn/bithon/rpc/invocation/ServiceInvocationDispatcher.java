package cn.bithon.rpc.invocation;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.message.ServiceRequestMessage;
import cn.bithon.rpc.message.ServiceResponseMessage;
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

    public void dispatch(Channel channel, ServiceRequestMessage serviceRequest) {
        executor.execute(new Invoker(om, channel, serviceRequest, serviceRegistry));
    }

    static class RejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Invoker invoker = (Invoker) r;

            sendResponse(invoker.channel, invoker.om, ServiceResponseMessage.builder()
                                                                            .serverResponseAt(System.currentTimeMillis())
                                                                            .transactionId(invoker.serviceRequest.getTransactionId())
                                                                            .exception(
                                                                         "Server has no enough resources to process the request.")
                                                                            .build());
        }
    }

    static void sendResponse(Channel channel, ObjectMapper om, ServiceResponseMessage serviceResponse) {
        channel.writeAndFlush(serviceResponse);
    }

    @AllArgsConstructor
    public static class Invoker implements Runnable {
        private final ObjectMapper om;
        private final Channel channel;
        private final ServiceRequestMessage serviceRequest;
        private final ServiceRegistry serviceRegistry;

        @Override
        public void run() {

            try {
                if (serviceRequest.getServiceName() == null) {
                    throw new BadRequestException("serviceName is null");
                }

                if (serviceRequest.getMethodName() == null) {
                    throw new BadRequestException("methodName is null");
                }

                ServiceRegistry.RpcServiceProvider serviceProvider = serviceRegistry.findServiceProvider(
                    serviceRequest.getServiceName(),
                    serviceRequest.getMethodName());
                if (serviceProvider == null) {
                    throw new BadRequestException("Can't find service provider %s#%s",
                                                  serviceRequest.getServiceName(),
                                                  serviceRequest.getMethodName());
                }

                Object[] inputArgs = parseArgs(serviceRequest.getServiceName(),
                                               serviceRequest.getMethodName(),
                                               serviceProvider.getParameterTypes());

                Object ret;
                try {
                    ret = serviceProvider.invoke(inputArgs);
                } catch (IllegalAccessException e) {
                    throw new ServiceInvocationException("Service[%s#%s] exception: %s",
                                                         serviceRequest.getServiceName(),
                                                         serviceRequest.getMethodName(),
                                                         e.getMessage());
                } catch (InvocationTargetException e) {
                    throw new ServiceInvocationException("Service[%s#%s] invocation exception: %s",
                                                         serviceRequest.getServiceName(),
                                                         serviceRequest.getMethodName(),
                                                         e.getTargetException().toString());
                }

                if (!serviceProvider.isReturnVoid()) {
                    sendResponse(ServiceResponseMessage.builder()
                                                       .serverResponseAt(System.currentTimeMillis())
                                                       .transactionId(serviceRequest.getTransactionId())
                                                       .returning(ret)
                                                       .build());
                }
            } catch (BadRequestException e) {
                sendResponse(ServiceResponseMessage.builder()
                                                   .serverResponseAt(System.currentTimeMillis())
                                                   .transactionId(serviceRequest.getTransactionId())
                                                   .exception(e.getMessage())
                                                   .build());
            } catch (ServiceInvocationException e) {
                sendResponse(ServiceResponseMessage.builder()
                                                   .serverResponseAt(System.currentTimeMillis())
                                                   .transactionId(serviceRequest.getTransactionId())
                                                   .exception(e.getMessage())
                                                   .build());
            }
        }

        private void sendResponse(ServiceResponseMessage serviceResponse) {
            ServiceInvocationDispatcher.sendResponse(channel, om, serviceResponse);
        }

        private Object[] parseArgs(CharSequence serviceName,
                                   CharSequence methodName,
                                   ServiceRegistry.ParameterType[] parameterTypes)
            throws BadRequestException {

            Object[] inputArgs = new Object[parameterTypes.length];
            if (parameterTypes.length <= 0) {
                return inputArgs;
            }

            JsonNode argsNode;
            try {
                argsNode = om.readTree(serviceRequest.getArgs());
            } catch (IOException e) {
                throw new BadRequestException("Can't deserialize args");
            }
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

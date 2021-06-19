package cn.bithon.rpc.invocation;

import cn.bithon.rpc.channel.IServiceChannel;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.exception.TimeoutException;
import cn.bithon.rpc.message.ServiceException;
import cn.bithon.rpc.message.ServiceMessageType;
import cn.bithon.rpc.message.ServiceRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manage inflight requests from a service client to a service provider
 */
@Slf4j
public class ServiceRequestManager {

    private static final ServiceRequestManager INSTANCE = new ServiceRequestManager();

    public static ServiceRequestManager getInstance() {
        return INSTANCE;
    }

    @Data
    static class InflightRequest {
        private String serviceName;
        private String methodName;
        long requestAt;
        long responseAt;
        Class<?> returnObjType;
        Object response;
        /**
         * indicate whether this request has response.
         * This is required so that {@link #response} might be null
         */
        boolean returned;
        ServiceException exception;
    }

    private final AtomicLong transactionId = new AtomicLong();
    private final ObjectMapper om = new JsonMapper();
    private final Map<Long, InflightRequest> inflightRequests = new ConcurrentHashMap<>();

    public Object invoke(IServiceChannel channelProvider, boolean debug, long timeout, Method method, Object[] args) {
        Channel ch = channelProvider.getChannel();
        if (ch == null) {
            throw new ServiceInvocationException("Failed to invoke %s#%s due to channel is empty",
                                                 method.getDeclaringClass().getSimpleName(),
                                                 method.getName());
        }
        if (!ch.isActive()) {
            throw new ServiceInvocationException("Failed to invoke %s#%s due to channel is not active",
                                                 method.getDeclaringClass().getSimpleName(),
                                                 method.getName());
        }

        ServiceRequest serviceRequest = ServiceRequest.builder()
                                                      .serviceName(method.getDeclaringClass().getSimpleName())
                                                      .methodName(method.getName())
                                                      .transactionId(transactionId.incrementAndGet())
                                                      .messageType(ServiceMessageType.CLIENT_REQUEST)
                                                      .args(args)
                                                      .build();
        Class<?> returnType = method.getReturnType();
        boolean isReturnVoid = returnType.equals(Void.TYPE);
        InflightRequest inflightRequest = null;
        if (!isReturnVoid) {
            inflightRequest = new InflightRequest();
            inflightRequest.requestAt = System.currentTimeMillis();
            inflightRequest.methodName = serviceRequest.getMethodName();
            inflightRequest.serviceName = serviceRequest.getServiceName();
            inflightRequest.returnObjType = returnType;
            this.inflightRequests.put(serviceRequest.getTransactionId(), inflightRequest);
        }
        try {
            channelProvider.writeAndFlush(om.writeValueAsString(serviceRequest));
        } catch (JsonProcessingException e) {
            throw new ServiceInvocationException("Failed to serialize service request due to: %s", e.getMessage());
        }
        if (inflightRequest != null) {
            try {
                synchronized (inflightRequest) {
                    inflightRequest.wait(timeout);
                }
            } catch (InterruptedException e) {
                inflightRequests.remove(serviceRequest.getTransactionId());
                throw new ServiceInvocationException("interrupted");
            }

            //make sure it has been cleared when timeout
            inflightRequests.remove(serviceRequest.getTransactionId());

            if (inflightRequest.exception != null) {
                throw new ServiceInvocationException(inflightRequest.exception.getMessage());
            }

            if (!inflightRequest.returned) {
                throw new TimeoutException(serviceRequest.getServiceName(),
                                           serviceRequest.getMethodName(),
                                           5000);
            }

            return inflightRequest.response;
        }
        return null;
    }

    public void onResponse(JsonNode responseNode) {
        JsonNode transactionId = responseNode.get("transactionId");
        if (transactionId == null || transactionId.isNull()) {
            return;
        }

        long txId = transactionId.asLong();
        InflightRequest inflightRequest = inflightRequests.remove(txId);
        if (inflightRequest == null) {
            return;
        }

        synchronized (inflightRequest) {
            JsonNode returningNode = responseNode.get("returning");
            if (returningNode != null && !returningNode.isNull()) {
                inflightRequest.response = om.convertValue(returningNode, inflightRequest.returnObjType);
            }

            JsonNode exceptionNode = responseNode.get("exception");
            if (exceptionNode != null && !exceptionNode.isNull()) {
                inflightRequest.exception = om.convertValue(exceptionNode, ServiceException.class);
            }

            inflightRequest.returned = true;

            inflightRequest.notify();
        }
    }
}

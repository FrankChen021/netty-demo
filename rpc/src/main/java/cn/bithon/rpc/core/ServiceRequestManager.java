package cn.bithon.rpc.core;

import cn.bithon.rpc.core.message.ServiceException;
import cn.bithon.rpc.core.message.ServiceMessageType;
import cn.bithon.rpc.core.message.ServiceRequest;
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
        ServiceException exception;
    }

    private final AtomicLong transactionId = new AtomicLong();
    private final ObjectMapper om = new JsonMapper();
    private final Map<Long, InflightRequest> inflightRequests = new ConcurrentHashMap<>();
    private final ThreadLocal<Integer> timeoutSetting = new InheritableThreadLocal<>();

    public void setCurrentTimeout(int timeout) {
        timeoutSetting.set(timeout);
    }

    public Object invoke(Channel channel, Method method, Object[] args) {
        ServiceRequest serviceRequest = ServiceRequest.builder()
                                                      .serviceName(method.getDeclaringClass().getSimpleName())
                                                      .methodName(method.getName())
                                                      .transactionId(transactionId.incrementAndGet())
                                                      .messageType(ServiceMessageType.CLIENT_REQUEST)
                                                      .args(args)
                                                      .build();
        log.info("sending client request:{}", serviceRequest);

        Class<?> returnType = method.getReturnType();
        boolean isReturnVoid = returnType.equals(Void.TYPE);
        InflightRequest pendingRequest = null;
        if (!isReturnVoid) {
            pendingRequest = new InflightRequest();
            pendingRequest.requestAt = System.currentTimeMillis();
            pendingRequest.methodName = serviceRequest.getMethodName();
            pendingRequest.serviceName = serviceRequest.getServiceName();
            pendingRequest.returnObjType = returnType;
            this.inflightRequests.put(serviceRequest.getTransactionId(), pendingRequest);
        }
        try {
            channel.writeAndFlush(om.writeValueAsString(serviceRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (pendingRequest != null) {
            try {
                Integer timeout = timeoutSetting.get();
                synchronized (pendingRequest) {
                    pendingRequest.wait(timeout == null ? 5000 : timeout);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (pendingRequest.exception != null) {
                throw new ServiceInvocationException(pendingRequest.exception.getMessage());
            }
            return pendingRequest.response;
        }
        return null;
    }

    public void onResponse(JsonNode responseNode) {
        JsonNode transactionId = responseNode.get("transactionId");
        if (transactionId == null || transactionId.isNull()) {
            return;
        }

        long txId = transactionId.asLong();
        InflightRequest inflightRequest = inflightRequests.get(txId);
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

            inflightRequest.notify();
        }
    }
}

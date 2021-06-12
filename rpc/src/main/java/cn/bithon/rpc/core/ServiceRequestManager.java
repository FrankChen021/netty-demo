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

@Slf4j
public class ServiceRequestManager {

    private static final ServiceRequestManager INSTANCE = new ServiceRequestManager();

    public static ServiceRequestManager getInstance() {
        return INSTANCE;
    }

    @Data
    public static class PendingRequest {
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
    private final Map<Long, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    public void onResponse(JsonNode responseNode) {
        JsonNode transactionId = responseNode.get("transactionId");
        if (transactionId == null || transactionId.isNull()) {
            return;
        }

        long txId = transactionId.asLong();
        PendingRequest pendingRequest = pendingRequests.get(txId);
        if (pendingRequest == null) {
            return;
        }

        synchronized (pendingRequest) {
            JsonNode returningNode = responseNode.get("returning");
            if (returningNode != null && !returningNode.isNull()) {
                pendingRequest.response = om.convertValue(returningNode, pendingRequest.returnObjType);
            }

            JsonNode exceptionNode = responseNode.get("exception");
            if (exceptionNode != null && !exceptionNode.isNull()) {
                pendingRequest.exception = om.convertValue(exceptionNode, ServiceException.class);
            }

            pendingRequest.notify();
        }
    }

    public Object sendServiceRequest(Channel channel, Method method, Object[] args) {
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
        PendingRequest pendingRequest = null;
        if (!isReturnVoid) {
            pendingRequest = new PendingRequest();
            pendingRequest.requestAt = System.currentTimeMillis();
            pendingRequest.methodName = serviceRequest.getMethodName();
            pendingRequest.serviceName = serviceRequest.getServiceName();
            pendingRequest.returnObjType = returnType;
            this.pendingRequests.put(serviceRequest.getTransactionId(), pendingRequest);
        }
        try {
            channel.writeAndFlush(om.writeValueAsString(serviceRequest));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if (pendingRequest != null) {
            try {
                synchronized (pendingRequest) {
                    pendingRequest.wait(5000);
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
}

package cn.bithon.rpc.core.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceRequest {
    private Long messageType;
    private String serviceName;
    private String methodName;
    private Long transactionId;
    private Object[] args;
}

package cn.bithon.rpc.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponse {
    /**
     * {@link ServiceMessageType}
     */
    private long messageType;
    private long transactionId;
    private long serverResponseAt;
    private Object returning;
    private ServiceException exception;
}

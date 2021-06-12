import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcResponse {
    private long messageType;
    private long transactionId;
    private long serverResponseAt;
    private Object returning;
    private RpcException exception;
}

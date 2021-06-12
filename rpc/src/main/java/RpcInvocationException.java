public class RpcInvocationException extends RuntimeException {
    public RpcInvocationException(String message) {
        super(message);
    }

    public RpcInvocationException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }
}

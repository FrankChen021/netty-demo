package cn.bithon.rpc.core;

public class ServiceInvocationException extends RuntimeException {
    public ServiceInvocationException(String message) {
        super(message);
    }

    public ServiceInvocationException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }
}

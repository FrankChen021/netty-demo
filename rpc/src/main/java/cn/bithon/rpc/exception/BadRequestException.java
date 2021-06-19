package cn.bithon.rpc.exception;

public class BadRequestException extends ServiceInvocationException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String messageFormat, Object... args) {
        super(messageFormat, args);
    }
}

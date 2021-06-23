package cn.bithon.rpc.exception;

/**
 * used only at server side for code simplification, and should not be used at client side
 */
public class BadRequestException extends ServiceInvocationException {
    public BadRequestException(String message) {
        super("Bad Request:" + message);
    }

    public BadRequestException(String messageFormat, Object... args) {
        super(messageFormat, args);
    }
}

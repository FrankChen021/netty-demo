package cn.bithon.rpc.core.exception;

import cn.bithon.rpc.core.exception.ServiceInvocationException;

public class BadRequestException extends ServiceInvocationException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String messageFormat, Object... args) {
        super(messageFormat, args);
    }
}

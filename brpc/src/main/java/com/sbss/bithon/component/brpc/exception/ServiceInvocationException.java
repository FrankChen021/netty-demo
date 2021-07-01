package com.sbss.bithon.component.brpc.exception;

public class ServiceInvocationException extends RuntimeException {
    public ServiceInvocationException(CharSequence message) {
        super(message instanceof String ? (String) message : message.toString());
    }

    public ServiceInvocationException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }
}

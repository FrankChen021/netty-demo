package com.sbss.bithon.component.brpc.message;

/**
 * @author frank.chen021@outlook.com
 * @date 2021/6/28 11:23 上午
 */
public class UnknownMessageException extends RuntimeException {
    public UnknownMessageException(int messageType) {
        super("Unknown message:" + messageType);
    }
}

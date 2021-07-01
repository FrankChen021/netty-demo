package com.sbss.bithon.component.brpc.message;

/**
 * 4: messageType
 * 4: serializer(JSON/Binary)
 * 8: transactionId
 */
public abstract class ServiceMessage {
    protected long transactionId;

    /**
     * {@link ServiceMessageType}
     */
    public abstract int getMessageType();

    public long getTransactionId() {
        return transactionId;
    }
}

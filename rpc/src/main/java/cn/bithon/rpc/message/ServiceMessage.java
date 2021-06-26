package cn.bithon.rpc.message;

/**
 * 4: messageType
 * 4: serializer(JSON/Binary)
 * 8: transactionId
 */
abstract public class ServiceMessage {
    protected long transactionId;

    /**
     * {@link ServiceMessageType}
     */
    abstract public int getMessageType();

    public long getTransactionId() {
        return transactionId;
    }
}

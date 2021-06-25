package cn.bithon.rpc.message;

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

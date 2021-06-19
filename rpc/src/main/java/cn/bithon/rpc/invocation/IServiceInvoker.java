package cn.bithon.rpc.invocation;

public interface IServiceInvoker {
    void debug(boolean on);

    /**
     * @param timeout milli-second
     */
    void setTimeout(long timeout);

    void rstTimeout();
}

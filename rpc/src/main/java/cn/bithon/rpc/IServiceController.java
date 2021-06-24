package cn.bithon.rpc;

public interface IServiceController {
    void debug(boolean on);

    /**
     * @param timeout milli-second
     */
    void setTimeout(long timeout);

    void rstTimeout();
}

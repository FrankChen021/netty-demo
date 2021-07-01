package com.sbss.bithon.component.brpc;

public interface IServiceController {
    void debug(boolean on);

    /**
     * @param timeout milli-second
     */
    void setTimeout(long timeout);

    void rstTimeout();
}

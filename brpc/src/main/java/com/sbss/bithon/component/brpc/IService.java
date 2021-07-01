package com.sbss.bithon.component.brpc;

public interface IService {

    default IServiceController toController() {
        return (IServiceController) this;
    }
}

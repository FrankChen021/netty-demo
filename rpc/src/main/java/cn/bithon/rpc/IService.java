package cn.bithon.rpc;

public interface IService {

    default IServiceController toController() {
        return (IServiceController) this;
    }
}

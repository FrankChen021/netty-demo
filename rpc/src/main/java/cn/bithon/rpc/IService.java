package cn.bithon.rpc;

import cn.bithon.rpc.invocation.IServiceInvoker;

public interface IService {

    static boolean isService(Class<?> type) {
        for (Class<?> superInterface : type.getInterfaces()) {
            if (superInterface.equals(IService.class)) {
                return true;
            }
        }
        return false;
    }

    default IServiceInvoker toInvoker() {
        return (IServiceInvoker) this;
    }
}

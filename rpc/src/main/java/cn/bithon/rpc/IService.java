package cn.bithon.rpc;

public interface IService {

    static boolean isService(Class<?> type) {
        for (Class<?> superInterface : type.getInterfaces()) {
            if (superInterface.equals(IService.class)) {
                return true;
            }
        }
        return false;
    }

    default IServiceHelper toInvoker() {
        return (IServiceHelper) this;
    }
}
